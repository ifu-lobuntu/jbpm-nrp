package org.jbpm.vdml.services;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.eclipse.emf.common.util.EList;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.jbpm.vdml.services.api.model.LinkedExternalObject;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemAvailability;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemRequirement;
import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.jbpm.vdml.services.impl.scheduling.SchedulingUtil;
import org.jbpm.vdml.services.scheduling.SchedulingUtilTest;
import org.joda.time.DateTime;
import org.junit.Test;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created by ampie on 2015/09/19.
 */
public class FindPooledResourceTest extends AbstractPoolExchangeTest {
    @Test
    public void testFind() throws Exception {
        TimerServiceRegistry.getInstance().registerTimerService(DEFAULT_DEPLOYMENT_ID, new PseudoClockScheduler());

        ValueDeliveryModel vdm = buildModel();
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = buildDefaultStoreExchange(vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant consumerParticipant = participantService.createIndividualParticipant("Consumer");
        EList<StoreDefinition> storeDefs = vdm.getStoreLibrary().get(0).getStoreDefinitions();
        List<String> storeDefIds = Arrays.asList(MetaBuilder.buildUri(findByName(storeDefs, "ResourcePool")));
        String tukTukDefinitionId = MetaBuilder.buildUri(findByName(vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement(), "TukTukDefinition"));
        IndividualParticipant supplier1 = createSupplier1(participantService, storeDefIds, tukTukDefinitionId);
        PoolPerformance pool1= (PoolPerformance) supplier1.getOfferedStores().iterator().next();
        ReusableBusinessItemPerformance resource1=pool1.getPooledResources().iterator().next();
        IndividualParticipant supplier2 = createSupplier2(participantService, storeDefIds, tukTukDefinitionId);
        PoolPerformance pool2= (PoolPerformance) supplier2.getOfferedStores().iterator().next();
        ReusableBusinessItemPerformance resource2=pool2.getPooledResources().iterator().next();
        IndividualParticipant supplier3 = createSupplier3(participantService, storeDefIds, tukTukDefinitionId);

        ExchangeService exchangeService = new ExchangeService(getEntityManager());

        //WHEN
        ReusableBusinessItemAvailability availability1 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId,0));
        //THEN
        assertEquals(resource1.getId(), availability1.getReusableBusinessItemId());
        exchangeService.scheduleReusableProductUse(consumerParticipant.getId(), availability1);
        ReusableBusinessItemAvailability availability2 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId,0));
        assertEquals(resource2.getId(), availability2.getReusableBusinessItemId());

        ReusableBusinessItemAvailability availability3 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId,3));
        exchangeService.scheduleReusableProductUse(consumerParticipant.getId(), availability2);
        assertEquals(resource1.getId(), availability3.getReusableBusinessItemId());
        exchangeService.scheduleReusableProductUse(consumerParticipant.getId(), availability3);
        ReusableBusinessItemAvailability availability4 = exchangeService.findAvailability(buildRequirement(tukTukDefinitionId,3));
        assertEquals(resource2.getId(), availability4.getReusableBusinessItemId());


    }

    protected IndividualParticipant createSupplier2(ParticipantService participantService, List<String> storeDefIds, String tukTukDefinitionId) {
        IndividualParticipant supplier2 = participantService.createIndividualParticipant("AlmostMatch");
        participantService.setAddress(supplier2.getId(), new Address(geometryFactory.createPoint(new Coordinate(LocationUtil.meterToEstimatedDegrees(3000),LocationUtil.meterToEstimatedDegrees(3000)))));
        participantService.setStores(supplier2.getId(), storeDefIds);
        Long bipId=participantService.addResourceToStore(participantService.findParticipant(supplier2.getId()).getOfferedStores().iterator().next().getId(), new LinkedExternalObject(tukTukDefinitionId,"TukTuk","AlmostMatch"));
        participantService.setResourceSchedule(bipId,SchedulingUtilTest.buildSchedule());
        return supplier2;
    }
    protected IndividualParticipant createSupplier3(ParticipantService participantService, List<String> storeDefIds, String tukTukDefinitionId) {
        IndividualParticipant supplier3 = participantService.createIndividualParticipant("TooFar");
        participantService.setAddress(supplier3.getId(), new Address(geometryFactory.createPoint(new Coordinate(LocationUtil.meterToEstimatedDegrees(20000),LocationUtil.meterToEstimatedDegrees(20000)))));
        participantService.setStores(supplier3.getId(), storeDefIds);
        Long bipId=participantService.addResourceToStore(participantService.findParticipant(supplier3.getId()).getOfferedStores().iterator().next().getId(), new LinkedExternalObject(tukTukDefinitionId,"TukTuk","TooFar"));
        participantService.setResourceSchedule(bipId,SchedulingUtilTest.buildSchedule());
        return supplier3;
    }


    //Cancel booking
    // 1. Remove PlannedUnavailability
    // 2. Unassociate PlannedUnavailability ProvidingPool and BusinessItemPerformance with ResourceUse,
    // 3. If PoolExchange then kill the exchange.

    //Commit to the booking
    // 1. Becomes contractually binding - could be automated X hours before, use Scheduler
    // 2. Allocate resource flows from non-pool stores - i.e. ProjectedInventoryLevels


    //Start - could be automated
    // 1. decrement InventoryLevel
    // 2. maybe some payments need to be made (deposit?

}
