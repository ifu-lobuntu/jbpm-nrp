package org.jbpm.vdml.services;

import org.drools.core.time.impl.PseudoClockScheduler;
import org.eclipse.emf.common.util.EList;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemAvailability;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemRequirement;
import org.jbpm.vdml.services.impl.ExchangeService;
import org.jbpm.vdml.services.impl.MetaBuilder;
import org.jbpm.vdml.services.impl.ParticipantService;
import org.jbpm.vdml.services.impl.model.meta.CapabilityMethod;
import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import org.omg.vdml.StoreLibraryElement;
import org.omg.vdml.ValueDeliveryModel;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PooledExchangeTest extends AbstractPoolExchangeTest {
    @Test
    public void testSchedule() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        EList<StoreLibraryElement> storeDefs = vdm.getStoreLibrary().get(0).getStoreLibraryElement();
        List<String> storeDefIds = Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ResourcePool")));
        String tukTukDefinitionId = MetaBuilder.buildUri(findByName(vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement(), "TukTukDefinition"));
        IndividualParticipant supplier1 = createSupplier1(participantService, storeDefIds, tukTukDefinitionId);
        PoolPerformance pool1= (PoolPerformance) supplier1.getOfferedStores().iterator().next();
        ReusableBusinessItemPerformance resource1=pool1.getPooledResources().iterator().next();

        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        ReusableBusinessItemAvailability availability1 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId,0));
        assertEquals(resource1.getId(), availability1.getReusableBusinessItemId());
        //WHEN
        exchangeService.scheduleReusableProductUse(consumerParticipant.getId(), availability1);
        //THEN
        ReusableBusinessItemAvailability availability2 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId,0));
        assertNull(availability2);

    }

    @Test
    public void testCancel() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        EList<StoreLibraryElement> storeDefs = vdm.getStoreLibrary().get(0).getStoreLibraryElement();
        List<String> storeDefIds = Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ResourcePool")));
        String tukTukDefinitionId = MetaBuilder.buildUri(findByName(vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement(), "TukTukDefinition"));
        IndividualParticipant supplier1 = createSupplier1(participantService, storeDefIds, tukTukDefinitionId);
        PoolPerformance pool1= (PoolPerformance) supplier1.getOfferedStores().iterator().next();
        ReusableBusinessItemPerformance resource1=pool1.getPooledResources().iterator().next();

        ExchangeService exchangeService = new ExchangeService(getEntityManager());

        ReusableBusinessItemAvailability availability1 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId,0));
        assertEquals(resource1.getId(), availability1.getReusableBusinessItemId());
        CollaborationInstance c = exchangeService.scheduleReusableProductUse(consumerParticipant.getId(), availability1);
        ReusableBusinessItemAvailability availability2 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId, 0));
        assertNull(availability2);
        //WHEN
        exchangeService.cancelBooking(c.getId());
        //THEN
        availability2 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId,0));
        assertEquals(resource1.getId(), availability2.getReusableBusinessItemId());

    }

    @Test
    public void testCommit() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.CapabilityMethod collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        EList<StoreLibraryElement> storeDefs = vdm.getStoreLibrary().get(0).getStoreLibraryElement();
        List<String> storeDefIds = Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ResourcePool")));
        String tukTukDefinitionId = MetaBuilder.buildUri(findByName(vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement(), "TukTukDefinition"));
        IndividualParticipant supplier1 = createSupplier1(participantService, storeDefIds, tukTukDefinitionId);
        PoolPerformance pool1= (PoolPerformance) supplier1.getOfferedStores().iterator().next();
        ReusableBusinessItemPerformance resource1=pool1.getPooledResources().iterator().next();

        ReusableBusinessItemRequirement requirement = buildRequirement(tukTukDefinitionId, 0);
        PseudoClockScheduler clock = new PseudoClockScheduler();
        long clockStart = requirement.getNotBefore().minusDays(2).getMillis();
        clock.setStartupTime(clockStart);
        TimerServiceRegistry.getInstance().registerTimerService(DEFAULT_DEPLOYMENT_ID, clock);
        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        ReusableBusinessItemAvailability availability1 = exchangeService.findAvailability(requirement);
        assertEquals(resource1.getId(), availability1.getReusableBusinessItemId());
        CollaborationInstance exchange = startExchangeAndSetAccountBalances(collaboration, consumerParticipant, exchangeService, availability1);
        ReusableBusinessItemAvailability availability2 = exchangeService.findAvailability(requirement);
        assertNull(availability2);
        //WHEN
        long millisUntilCommitment= availability1.getFrom().minusDays(1).getMillis() - clock.getCurrentTime();
        clock.advanceTime(millisUntilCommitment, TimeUnit.MILLISECONDS);
        //THEN
        CollaborationInstance foundExchange=new ExchangeService(getEntityManager()).findExchange(exchange.getId());
        StorePerformance fromAccount = foundExchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("FromAccount")).getStore();
        StorePerformance toAccount = foundExchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("ToAccount")).getStore();
        Assert.assertEquals(1000d, fromAccount.getInventoryLevel(), 0.01);
        Assert.assertEquals(900d, fromAccount.getProjectedInventoryLevel(), 0.01);
        Assert.assertEquals(2000d, toAccount.getInventoryLevel(), 0.01);
        Assert.assertEquals(2100d, toAccount.getProjectedInventoryLevel(), 0.01);

    }
    @Test
    public void testFulfill() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.CapabilityMethod collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        EList<StoreLibraryElement> storeDefs = vdm.getStoreLibrary().get(0).getStoreLibraryElement();
        List<String> storeDefIds = Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ResourcePool")));
        String tukTukDefinitionId = MetaBuilder.buildUri(findByName(vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement(), "TukTukDefinition"));
        IndividualParticipant supplier1 = createSupplier1(participantService, storeDefIds, tukTukDefinitionId);
        PoolPerformance pool1= (PoolPerformance) supplier1.getOfferedStores().iterator().next();
        ReusableBusinessItemPerformance resource1=pool1.getPooledResources().iterator().next();

        ReusableBusinessItemRequirement requirement = buildRequirement(tukTukDefinitionId, 0);
        PseudoClockScheduler clock = new PseudoClockScheduler();
        long clockStart = requirement.getNotBefore().minusDays(2).getMillis();
        clock.setStartupTime(clockStart);
        TimerServiceRegistry.getInstance().registerTimerService(DEFAULT_DEPLOYMENT_ID, clock);
        ExchangeService exchangeService = new ExchangeService(getEntityManager());
        ReusableBusinessItemAvailability availability1 = exchangeService.findAvailability(requirement);
        assertEquals(resource1.getId(), availability1.getReusableBusinessItemId());
        CollaborationInstance exchange = startExchangeAndSetAccountBalances(collaboration, consumerParticipant, exchangeService, availability1);
        ReusableBusinessItemAvailability availability2 = exchangeService.findAvailability(requirement);
        assertNull(availability2);
        //WHEN
        long millisUntilFulfillment= availability1.getTo().getMillis() - clock.getCurrentTime();
        clock.advanceTime(millisUntilFulfillment, TimeUnit.MILLISECONDS);
        //THEN
        CollaborationInstance foundExchange=new ExchangeService(getEntityManager()).findExchange(exchange.getId());
        StorePerformance fromAccount = foundExchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("FromAccount")).getStore();
        StorePerformance toAccount = foundExchange.findFirstSupplyingStore(exchange.getCollaboration().findSupplyingStore("ToAccount")).getStore();
        Assert.assertEquals(900d, fromAccount.getInventoryLevel(), 0.01);
        Assert.assertEquals(900d, fromAccount.getProjectedInventoryLevel(), 0.01);
        Assert.assertEquals(2100d, toAccount.getInventoryLevel(), 0.01);
        Assert.assertEquals(2100d, toAccount.getProjectedInventoryLevel(), 0.01);

    }

    protected CollaborationInstance startExchangeAndSetAccountBalances(CapabilityMethod collaboration, IndividualParticipant consumerParticipant, ExchangeService exchangeService, ReusableBusinessItemAvailability availability1) {
        CollaborationInstance exchange = exchangeService.scheduleReusableProductUse(consumerParticipant.getId(), availability1);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().setProjectedInventoryLevel(1000d);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().setProjectedInventoryLevel(2000d);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("FromAccount")).getStore().setInventoryLevel(1000d);
        exchange.findFirstSupplyingStore(collaboration.findSupplyingStore("ToAccount")).getStore().setInventoryLevel(2000d);
        for (DeliverableFlowInstance flow : exchange.getOwnedDirectedFlows()) {
            if(flow.getDeliverable().getDefinition().getName().equals("Money")){
                flow.getQuantity().setActualValue(100d);
            }
        }
        exchangeService.flush();
        return exchange;
    }
}
