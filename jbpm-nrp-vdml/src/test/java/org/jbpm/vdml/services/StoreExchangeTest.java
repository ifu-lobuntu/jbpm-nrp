package org.jbpm.vdml.services;

import org.eclipse.emf.common.util.EList;
import org.jbpm.vdml.services.impl.ExchangeService;
import org.jbpm.vdml.services.impl.MetaBuilder;
import org.jbpm.vdml.services.impl.ParticipantService;
import org.jbpm.vdml.services.impl.VdmlImporter;
import org.jbpm.vdml.services.impl.model.runtime.CollaborationInstance;
import org.jbpm.vdml.services.impl.model.runtime.DeliverableFlowInstance;
import org.jbpm.vdml.services.impl.model.runtime.IndividualParticipant;
import org.jbpm.vdml.services.impl.model.runtime.StorePerformance;
import org.junit.Test;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StoreExchangeTest extends MetaEntityImportTest {
    @Test
    public void testExchange() throws Exception{
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.CapabilityMethod collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        IndividualParticipant supplierParticipant = participantService.createIndividualParticipant("StoreOwner");
        EList<StoreLibraryElement> storeDefs = vdm.getStoreLibrary().get(0).getStoreLibraryElement();

        participantService.setStores(supplierParticipant.getId(), Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ProductStore"))));
        ExchangeService exchangeService = new ExchangeService(getEntityManager());

        //WHEN
        CollaborationInstance exchange = exchangeService.startExchangeForProduct(consumerParticipant.getId(), supplierParticipant.getOfferedStores().iterator().next().getId());
        //THEN
        assertEquals(3, exchange.getActivities().size());
        assertNotNull(exchange.findFirstActivity(collaboration.findActivity("Request")));
        assertNotNull(exchange.findFirstActivity(collaboration.findActivity("Receive")));
        assertNotNull(exchange.findFirstActivity(collaboration.findActivity("Provide")));
        assertEquals(2, exchange.getCollaborationRoles().size());
        assertNotNull(exchange.findRole(collaboration.findRole("Consumer")));
        assertNotNull(exchange.findRole(collaboration.findRole("Provider")));
        assertEquals(3, exchange.getSupplyingStores().size());
        assertNotNull(exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("FromAccount")));
        assertNotNull(exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ToAccount")));
        assertNotNull(exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ProductStore")));

        assertEquals(consumerParticipant.getId(), exchange.findRole(collaboration.findRole("Consumer")).getParticipant().getId());
        assertEquals(supplierParticipant.getId(), exchange.findRole(collaboration.findRole("Provider")).getParticipant().getId());
        assertEquals(consumerParticipant.getId(), exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().getOwner().getId());
        assertEquals(supplierParticipant.getId(), exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().getOwner().getId());
        assertEquals(supplierParticipant.getId(), exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ProductStore")).getStore().getOwner().getId());
        assertEquals(consumerParticipant.getId(), exchange.findFirstActivity(collaboration.findActivity("Request")).getCapabilityOffer().getParticipant().getId());
        assertEquals(supplierParticipant.getId(), exchange.findFirstActivity(collaboration.findActivity("Provide")).getCapabilityOffer().getParticipant().getId());
        assertEquals(consumerParticipant.getId(), exchange.findFirstActivity(collaboration.findActivity("Receive")).getCapabilityOffer().getParticipant().getId());
    }
    @Test
    public void testCommit() throws Exception{
        Long exchangeId = startExchangeAndProvideQuantities();
        //WHEN
        new ExchangeService(getEntityManager()).commitToExchange(exchangeId);
        //THEN
        CollaborationInstance exchange=new ExchangeService(getEntityManager()).findExchange(exchangeId);
        StorePerformance fromAccount = exchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("FromAccount")).getStore();
        StorePerformance toAccount = exchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("ToAccount")).getStore();
        StorePerformance fromStore = exchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("ProductStore")).getStore();
        assertEquals(1000d, fromAccount.getInventoryLevel(),0.01);
        assertEquals(900d, fromAccount.getProjectedInventoryLevel(),0.01);
        assertEquals(2000d, toAccount.getInventoryLevel(),0.01);
        assertEquals(2100d, toAccount.getProjectedInventoryLevel(),0.01);
        assertEquals(100d, fromStore.getInventoryLevel(),0.01);
        assertEquals(95d, fromStore.getProjectedInventoryLevel(),0.01);
    }
    @Test
    public void testFulfill() throws Exception{
        Long exchangeId = startExchangeAndProvideQuantities();
        ParticipantService participantService;
        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        exchangeService.commitToExchange(exchangeId);
        exchangeService.fulfillExchange(exchangeId);
        //THEN
        CollaborationInstance exchange=new ExchangeService(getEntityManager()).findExchange(exchangeId);
        StorePerformance fromAccount = exchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("FromAccount")).getStore();
        StorePerformance toAccount = exchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("ToAccount")).getStore();
        StorePerformance fromStore = exchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("ProductStore")).getStore();
        assertEquals(900d, fromAccount.getInventoryLevel(),0.01);
        assertEquals(900d, fromAccount.getProjectedInventoryLevel(),0.01);
        assertEquals(2100d, toAccount.getInventoryLevel(), 0.01);
        assertEquals(2100d, toAccount.getProjectedInventoryLevel(), 0.01);
        assertEquals(95d, fromStore.getProjectedInventoryLevel(), 0.01);
        assertEquals(95d, fromStore.getInventoryLevel(), 0.01);
    }

    protected Long startExchangeAndProvideQuantities() throws IOException {
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.CapabilityMethod collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        IndividualParticipant supplierParticipant = participantService.createIndividualParticipant("StoreOwner");
        EList<StoreLibraryElement> storeDefs = vdm.getStoreLibrary().get(0).getStoreLibraryElement();

        participantService.setStores(supplierParticipant.getId(), Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ProductStore"))));
        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        CollaborationInstance exchange = exchangeService.startExchangeForProduct(consumerParticipant.getId(), supplierParticipant.getOfferedStores().iterator().next().getId());
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().setProjectedInventoryLevel(1000d);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().setProjectedInventoryLevel(2000d);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().setInventoryLevel(1000d);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().setInventoryLevel(2000d);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ProductStore")).getStore().setProjectedInventoryLevel(100d);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ProductStore")).getStore().setInventoryLevel(100d);
        for (DeliverableFlowInstance flow : exchange.getOwnedDirectedFlows()) {
            if(flow.getDeliverable().getDefinition().getName().equals("Money")){
                flow.getQuantity().setActualValue(100d);
            }else if(flow.getDeliverable().getDefinition().getName().equals("ProductDefinition")){
                flow.getQuantity().setActualValue(5d);
            }
        }
        exchangeService.flush();
        return exchange.getId();
    }

    protected org.jbpm.vdml.services.impl.model.meta.CapabilityMethod buildDefaultStoreExchange(ValueDeliveryModel vdm) throws IOException {
        BusinessItemDefinition money = createBusinessItemDefinition(vdm, "Money");


        StoreDefinition account = createStore(vdm, money, "Account");
        Characteristic amount = buildDirectMeasure(vdm, "Amount");
        account.setInventoryLevel(amount);

        BusinessItemDefinition productDefinition = createBusinessItemDefinition(vdm, "ProductDefinition");

        StoreDefinition store = createStore(vdm, productDefinition, "ProductStore");
        store.setInventoryLevel(buildDirectMeasure(vdm, "InventoryLevel"));

        BusinessItemDefinition orderDefinition = createBusinessItemDefinition(vdm, "OrderDefinition");

        CapabilityDefinition requestDef = createCapabilityDefinition(vdm, "Request");

        CapabilityDefinition provideDef = createCapabilityDefinition(vdm, "Provide");

        CapabilityDefinition receiveDef = createCapabilityDefinition(vdm, "Receive");

        CapabilityMethod cp = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cp);
        cp.setName("ProvideProductExchange");
        BusinessItem productBusinessItem = addBusinessItem(productDefinition, cp);
        BusinessItem orderBusinessItem = addBusinessItem(orderDefinition, cp);
        BusinessItem moneyBusinessItem = addBusinessItem(money, cp);
        OrgUnit network=createValueNetwork(vdm, "Newtokr");
        Role storeOwner = createRole(cp,network, "Provider");

        Role consumer = createRole(cp, network,"Consumer");

        SupplyingStore fromAccount = addSupplyingStore(cp, account, consumer, "FromAccount", "amount");

        SupplyingStore toAccount = addSupplyingStore(cp, account, storeOwner, "ToAccount", "amount");

        SupplyingStore productStore= addSupplyingStore(cp, store, storeOwner, "ProductStore", "inventoryLevel");

        Activity requestProduct = addActivity(requestDef, cp, consumer, "Request");

        Activity provideProduct = addActivity(provideDef, cp, storeOwner, "Provide");


        Activity receiveProduct = addActivity(receiveDef,cp,consumer,"Receive");

        Milestone productComplete = VDMLFactory.eINSTANCE.createMilestone();
        productComplete.setName("ProductComplete");
        cp.getMilestone().add(productComplete);

        store.setExchangeConfiguration(VDMLFactory.eINSTANCE.createExchangeConfiguration());
        store.getExchangeConfiguration().setExchangeMethod(cp);
        store.getExchangeConfiguration().setExchangeMilestone(productComplete);
        store.getExchangeConfiguration().setSupplierRole(storeOwner);


        addDeliverableFlow(cp, orderBusinessItem, requestProduct, provideProduct, "providedWorkDefinition", "receivedWorkDefinition");
        addDeliverableFlow(cp, moneyBusinessItem, fromAccount, provideProduct, "paidMoney", "receivedMoney").setMilestone(productComplete);
        addDeliverableFlow(cp, moneyBusinessItem, provideProduct, toAccount, "receivedMoney", "savedMoney").setMilestone(productComplete);
        addDeliverableFlow(cp, productBusinessItem, productStore, receiveProduct, "productSold", "productBought").setMilestone(productComplete);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        VdmlImporter vi = new VdmlImporter(getEntityManager());
        vi.buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = vi.buildCollaboration(DEFAULT_DEPLOYMENT_ID, cp);
        return (org.jbpm.vdml.services.impl.model.meta.CapabilityMethod) collaboration;
    }




}
