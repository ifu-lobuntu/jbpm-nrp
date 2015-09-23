package org.jbpm.vdml.services;

import org.eclipse.emf.common.util.EList;
import org.jbpm.vdml.services.impl.ExchangeService;
import org.jbpm.vdml.services.impl.MetaBuilder;
import org.jbpm.vdml.services.impl.ParticipantService;
import org.jbpm.vdml.services.impl.VdmlImporter;
import org.jbpm.vdml.services.impl.model.runtime.CollaborationObservation;
import org.jbpm.vdml.services.impl.model.runtime.DirectedFlowObservation;
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

public class StoreExchangeTest extends AbstractExchangeTest {
    @Test
    public void testExchange() throws Exception{
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        IndividualParticipant supplierParticipant = participantService.createIndividualParticipant("StoreOwner");
        EList<StoreDefinition> storeDefs = vdm.getStoreLibrary().get(0).getStoreDefinitions();

        participantService.setStores(supplierParticipant.getId(), Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ProductStore"))));
        ExchangeService exchangeService = new ExchangeService(getEntityManager());

        //WHEN
        CollaborationObservation exchange = exchangeService.startExchangeForProduct(consumerParticipant.getId(), supplierParticipant.getOfferedStores().iterator().next().getId());
        //THEN
        assertEquals(3, exchange.getActivities().size());
        assertNotNull(exchange.findActivity(collaboration.findActivity("Request")));
        assertNotNull(exchange.findActivity(collaboration.findActivity("Receive")));
        assertNotNull(exchange.findActivity(collaboration.findActivity("Provide")));
        assertEquals(2, exchange.getCollaborationRoles().size());
        assertNotNull(exchange.findRole(collaboration.findRole("Consumer")));
        assertNotNull(exchange.findRole(collaboration.findRole("Provider")));
        assertEquals(3, exchange.getSupplyingStores().size());
        assertNotNull(exchange.findSupplyingStore(collaboration.findSupplyingStore("FromAccount")));
        assertNotNull(exchange.findSupplyingStore(collaboration.findSupplyingStore("ToAccount")));
        assertNotNull(exchange.findSupplyingStore(collaboration.findSupplyingStore("ProductStore")));

        assertEquals(consumerParticipant.getId(), exchange.findRole(collaboration.findRole("Consumer")).getParticipant().getId());
        assertEquals(supplierParticipant.getId(), exchange.findRole(collaboration.findRole("Provider")).getParticipant().getId());
        assertEquals(consumerParticipant.getId(), exchange.findSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().getOwner().getId());
        assertEquals(supplierParticipant.getId(), exchange.findSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().getOwner().getId());
        assertEquals(supplierParticipant.getId(), exchange.findSupplyingStore(collaboration.findSupplyingStore("ProductStore")).getStore().getOwner().getId());
        assertEquals(consumerParticipant.getId(), exchange.findActivity(collaboration.findActivity("Request")).getCapabilityOffer().getParticipant().getId());
        assertEquals(supplierParticipant.getId(), exchange.findActivity(collaboration.findActivity("Provide")).getCapabilityOffer().getParticipant().getId());
        assertEquals(consumerParticipant.getId(), exchange.findActivity(collaboration.findActivity("Receive")).getCapabilityOffer().getParticipant().getId());
    }
    @Test
    public void testCommit() throws Exception{
        Long exchangeId = startExchangeAndProvideQuantities();
        //WHEN
        new ExchangeService(getEntityManager()).commitToExchange(exchangeId);
        //THEN
        CollaborationObservation exchange=new ExchangeService(getEntityManager()).findExchange(exchangeId);
        StorePerformance fromAccount = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("FromAccount")).getStore();
        StorePerformance toAccount = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("ToAccount")).getStore();
        StorePerformance fromStore = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("ProductStore")).getStore();
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
        CollaborationObservation exchange=new ExchangeService(getEntityManager()).findExchange(exchangeId);
        StorePerformance fromAccount = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("FromAccount")).getStore();
        StorePerformance toAccount = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("ToAccount")).getStore();
        StorePerformance fromStore = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("ProductStore")).getStore();
        assertEquals(900d, fromAccount.getInventoryLevel(),0.01);
        assertEquals(900d, fromAccount.getProjectedInventoryLevel(),0.01);
        assertEquals(2100d, toAccount.getInventoryLevel(), 0.01);
        assertEquals(2100d, toAccount.getProjectedInventoryLevel(), 0.01);
        assertEquals(95d, fromStore.getProjectedInventoryLevel(), 0.01);
        assertEquals(95d, fromStore.getInventoryLevel(), 0.01);
    }

    protected Long startExchangeAndProvideQuantities() throws IOException {
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        IndividualParticipant supplierParticipant = participantService.createIndividualParticipant("StoreOwner");
        EList<StoreDefinition> storeDefs = vdm.getStoreLibrary().get(0).getStoreDefinitions();

        participantService.setStores(supplierParticipant.getId(), Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ProductStore"))));
        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        CollaborationObservation exchange = exchangeService.startExchangeForProduct(consumerParticipant.getId(), supplierParticipant.getOfferedStores().iterator().next().getId());
        exchange.findSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().setProjectedInventoryLevel(1000d);
        exchange.findSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().setProjectedInventoryLevel(2000d);
        exchange.findSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().setInventoryLevel(1000d);
        exchange.findSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().setInventoryLevel(2000d);
        exchange.findSupplyingStore(collaboration.findSupplyingStore("ProductStore")).getStore().setProjectedInventoryLevel(100d);
        exchange.findSupplyingStore(collaboration.findSupplyingStore("ProductStore")).getStore().setInventoryLevel(100d);
        for (DirectedFlowObservation flow : exchange.getOwnedDirectedFlows()) {
            if(flow.getDeliverable().getBusinessItemDefinition().getName().equals("Money")){
                flow.getQuantity().setValue(100d);
            }else if(flow.getDeliverable().getBusinessItemDefinition().getName().equals("ProductDefinition")){
                flow.getQuantity().setValue(5d);
            }
        }
        exchangeService.flush();
        return exchange.getId();
    }

    protected org.jbpm.vdml.services.impl.model.meta.Collaboration buildDefaultStoreExchange(ValueDeliveryModel vdm) throws IOException {
        BusinessItemDefinition money = createMoney(vdm);


        StoreDefinition account = createAccount(vdm, money);
        Characteristic amount = createAmount(vdm);
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

        Role storeOwner = createRole(cp, "Provider");

        Role consumer = createRole(cp, "Consumer");

        SupplyingStore fromAccount = createFromAccount(account, cp, consumer);

        SupplyingStore toAccount = createToAccount(account, cp, storeOwner);

        SupplyingStore productStore= createSupplyingStore(store, cp, storeOwner, "ProductStore", "inventoryLevel");

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
        return collaboration;
    }




}
