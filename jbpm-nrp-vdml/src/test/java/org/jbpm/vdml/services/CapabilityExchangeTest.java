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

public class CapabilityExchangeTest extends AbstractExchangeTest {
    @Test
    public void testCapabilityExchange() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = buildDefaultCapabilityExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        IndividualParticipant supplierParticipant = participantService.createIndividualParticipant("CapabilityProvider");
        EList<Capability> capabilities = vdm.getCapabilitylibrary().get(0).getCapability();

        participantService.setCapabilities(supplierParticipant.getId(), Arrays.asList(MetaBuilder.buildUri(findByName(capabilities, "DoWork"))));
        ExchangeService exchangeService = new ExchangeService(getEntityManager());

        //WHEN
        CollaborationObservation exchange = exchangeService.startExchangeForService(consumerParticipant.getId(), supplierParticipant.getCapabilityOffers().iterator().next().getId());
        //THEN
        assertEquals(2, exchange.getActivities().size());
        assertNotNull(exchange.findActivity(collaboration.findActivity("DefineWork")));
        assertNotNull(exchange.findActivity(collaboration.findActivity("DoWork")));
        assertEquals(2, exchange.getCollaborationRoles().size());
        assertNotNull(exchange.findRole(collaboration.findRole("Consumer")));
        assertNotNull(exchange.findRole(collaboration.findRole("Provider")));
        assertEquals(2, exchange.getSupplyingStores().size());
        assertNotNull(exchange.findSupplyingStore(collaboration.findSupplyingStore("FromAccount")));
        assertNotNull(exchange.findSupplyingStore(collaboration.findSupplyingStore("ToAccount")));
        assertEquals(consumerParticipant.getId(), exchange.findRole(collaboration.findRole("Consumer")).getParticipant().getId());
        assertEquals(supplierParticipant.getId(), exchange.findRole(collaboration.findRole("Provider")).getParticipant().getId());
        assertEquals(consumerParticipant.getId(), exchange.findSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().getOwner().getId());
        assertEquals(supplierParticipant.getId(), exchange.findSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().getOwner().getId());
        assertEquals(consumerParticipant.getId(), exchange.findActivity(collaboration.findActivity("DefineWork")).getCapabilityOffer().getParticipant().getId());
        assertEquals(supplierParticipant.getId(), exchange.findActivity(collaboration.findActivity("DoWork")).getCapabilityOffer().getParticipant().getId());
    }

    @Test
    public void testCommit() throws Exception {
        //GIVEN
        Long exchangeId = startExchangeAndProvideQuantities();
        ParticipantService participantService;
        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        //WHEN
        exchangeService.commitToExchange(exchangeId);
        //THEN
        CollaborationObservation exchange = new ExchangeService(getEntityManager()).findExchange(exchangeId);
        StorePerformance fromAccount = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("FromAccount")).getStore();
        StorePerformance toAccount = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("ToAccount")).getStore();
        assertEquals(1000d, fromAccount.getInventoryLevel(), 0.01);
        assertEquals(900d, fromAccount.getProjectedInventoryLevel(), 0.01);
        assertEquals(2000d, toAccount.getInventoryLevel(), 0.01);
        assertEquals(2100d, toAccount.getProjectedInventoryLevel(), 0.01);
    }

    @Test
    public void testFulfill() throws Exception {
        //GIVEN
        Long exchangeId = startExchangeAndProvideQuantities();
        ParticipantService participantService;
        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        exchangeService.commitToExchange(exchangeId);
        //WHEN
        exchangeService.fulfillExchange(exchangeId);
        //THEN
        CollaborationObservation exchange = new ExchangeService(getEntityManager()).findExchange(exchangeId);
        StorePerformance fromAccount = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("FromAccount")).getStore();
        StorePerformance toAccount = exchange.findSupplyingStore(exchange.getCollaboration().findSupplyingStore("ToAccount")).getStore();
        assertEquals(900d, fromAccount.getInventoryLevel(), 0.01);
        assertEquals(900d, fromAccount.getProjectedInventoryLevel(), 0.01);
        assertEquals(2100d, toAccount.getInventoryLevel(), 0.01);
        assertEquals(2100d, toAccount.getProjectedInventoryLevel(), 0.01);
    }

    protected Long startExchangeAndProvideQuantities() throws IOException {
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = buildDefaultCapabilityExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        IndividualParticipant supplierParticipant = participantService.createIndividualParticipant("CapabilityProvider");
        EList<Capability> capabilities = vdm.getCapabilitylibrary().get(0).getCapability();

        participantService.setCapabilities(supplierParticipant.getId(), Arrays.asList(MetaBuilder.buildUri(findByName(capabilities, "DoWork"))));
        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        CollaborationObservation exchange = exchangeService.startExchangeForService(consumerParticipant.getId(), supplierParticipant.getCapabilityOffers().iterator().next().getId());
        exchange.findSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().setProjectedInventoryLevel(1000d);
        exchange.findSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().setProjectedInventoryLevel(2000d);
        exchange.findSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().setInventoryLevel(1000d);
        exchange.findSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().setInventoryLevel(2000d);
        //WHEN
        for (DirectedFlowObservation flow : exchange.getOwnedDirectedFlows()) {
            if (flow.getDeliverable().getBusinessItemDefinition().getName().equals("Money")) {
                flow.getQuantity().setValue(100d);
            }
        }
        exchangeService.flush();
        return exchange.getId();
    }

    protected org.jbpm.vdml.services.impl.model.meta.Collaboration buildDefaultCapabilityExchange(ValueDeliveryModel vdm) throws IOException {
        BusinessItemDefinition money = createMoney(vdm);


        StoreDefinition account = createAccount(vdm, money);
        Characteristic amount = createAmount(vdm);
        account.setInventoryLevel(amount);

        BusinessItemDefinition workDefinition = createBusinessItemDefinition(vdm, "WorkDefinition");

        CapabilityDefinition doWorkDef = createCapabilityDefinition(vdm, "DoWork");

        CapabilityDefinition defineWorkDef = createCapabilityDefinition(vdm,"DefineWork");

        CapabilityMethod cp = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cp);
        cp.setName("DoWorkExchange");
        BusinessItem workBusinessItem = addBusinessItem(workDefinition, cp);
        BusinessItem moneyBusinessItem = addBusinessItem(money, cp);


        Role capabilityProvider = createRole(cp,"Provider");

        Role consumer = createRole(cp, "Consumer");

        SupplyingStore fromAccount = createFromAccount(account, cp, consumer);

        SupplyingStore toAccount = createToAccount(account, cp, capabilityProvider);

        Activity defineWork = addActivity(defineWorkDef,cp,consumer,"DefineWork");


        Activity doWork = addActivity(doWorkDef,cp, capabilityProvider, "DoWork");


        Milestone workComplete = VDMLFactory.eINSTANCE.createMilestone();
        workComplete.setName("WorkComplete");
        cp.getMilestone().add(workComplete);

        doWorkDef.setExchangeConfiguration(VDMLFactory.eINSTANCE.createExchangeConfiguration());
        doWorkDef.getExchangeConfiguration().setExchangeMethod(cp);
        doWorkDef.getExchangeConfiguration().setExchangeMilestone(workComplete);
        doWorkDef.getExchangeConfiguration().setSupplierRole(capabilityProvider);


        addDeliverableFlow(cp, workBusinessItem, defineWork, doWork, "providedWorkDefinition", "receivedWorkDefinition");
        addDeliverableFlow(cp, moneyBusinessItem, fromAccount, doWork, "paidMoney", "receivedMoney").setMilestone(workComplete);
        addDeliverableFlow(cp, moneyBusinessItem, doWork, toAccount, "receivedMoney", "savedMoney").setMilestone(workComplete);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        VdmlImporter vi = new VdmlImporter(getEntityManager());
        vi.buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = vi.buildCollaboration(DEFAULT_DEPLOYMENT_ID, cp);
        return collaboration;
    }


}
