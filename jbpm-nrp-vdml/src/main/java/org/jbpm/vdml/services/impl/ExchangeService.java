package org.jbpm.vdml.services.impl;

import org.drools.core.time.JobHandle;
import org.drools.core.time.TimerService;
import org.drools.core.time.impl.PointInTimeTrigger;
import org.jbpm.process.core.timer.TimerServiceRegistry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.lang3.time.StopWatch;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemAvailability;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemRequirement;
import org.jbpm.vdml.services.impl.model.meta.Capability;
import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.jbpm.vdml.services.impl.model.scheduling.PlannedUnavailability;
import org.jbpm.vdml.services.impl.scheduling.Booking;
import org.jbpm.vdml.services.impl.scheduling.BookingSolution;
import org.jbpm.vdml.services.impl.scheduling.ScheduleSlot;
import org.jbpm.vdml.services.impl.scheduling.SchedulingUtil;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUtil;
import javax.persistence.Query;
import java.util.*;

/**
 * As a consumer/supplier, I would like to exchange value with another in order to gain a benefit from the network. Isolated exchanges based on a single Store, Pool or Capability
 */
public class ExchangeService extends AbstractRuntimeService {
    private CollaborationService collaborationService;

    public ExchangeService() {
    }

    public ExchangeService(EntityManager em) {
        super(em);
        this.collaborationService = new CollaborationService(em);
    }

    public void commitToExchange(Long collaborationId) {
        MilestoneInstance mo = findExchangeMilestone(collaborationId);
        collaborationService.commitToMilestone(mo);
    }

    public void fulfillExchange(Long collaborationId) {
        MilestoneInstance mo = findExchangeMilestone(collaborationId);
        collaborationService.fulfillMilestone(mo);
    }

    protected MilestoneInstance findExchangeMilestone(Long collaborationId) {
        CollaborationInstance collaborationInstance = entityManager.find(CollaborationInstance.class, collaborationId);
        DirectlyExchangable exchangeOffering = findExchangeOffering(collaborationInstance);
        MilestoneInstance mo = null;
        if (exchangeOffering != null) {
            mo = collaborationInstance.findMilestone(exchangeOffering.getExchangeConfiguration().getExchangeMilestone());
        }
        return mo;
    }

    private DirectlyExchangable findExchangeOffering(CollaborationInstance collaborationInstance) {
        DirectlyExchangable result = null;
        Collection<DirectlyExchangable> offers = new ArrayList<DirectlyExchangable>();
        offers.addAll(collaborationInstance.getStoresUsed());
        offers.addAll(collaborationInstance.getCapabilityOffersUsed());

        for (DirectlyExchangable offer : offers) {
            ExchangeConfiguration ec = offer.getExchangeConfiguration();
            if (ec != null && ec.getCollaborationToUse().equals(collaborationInstance.getCollaboration())) {
                if (offer.getSupplier().equals(collaborationInstance.findRole(ec.getSupplierRole()).getParticipant())) {
                    result = offer;
                    break;
                }
            }
        }
        return result;
    }

    public CollaborationInstance startExchangeForService(Long requestorId, Long capabilityPerformanceId) {
        CapabilityOffer cp = entityManager.find(CapabilityOffer.class, capabilityPerformanceId);
        Participant participant = entityManager.find(Participant.class, requestorId);
        Capability cpd = cp.getCapability();
        RolePerformance requestorRolePerformance = findOrCreateRole(participant, cpd.getExchangeConfiguration().getCollaborationToUse().getInitiatorRole());
        RolePerformance supplierRolePerformance = findOrCreateRole(cp.getParticipant(), cpd.getExchangeConfiguration().getSupplierRole());
        return collaborationService.startCollaboration(cpd.getExchangeConfiguration().getCollaborationToUse(), Arrays.asList(requestorRolePerformance, supplierRolePerformance));
    }

    public CollaborationInstance startExchangeForProduct(Long requestorId, Long storePerformanceId) {
        StorePerformance cp = entityManager.find(StorePerformance.class, storePerformanceId);
        Participant participant = entityManager.find(Participant.class, requestorId);
        RolePerformance requestorRolePerformance = findOrCreateRole(participant, cp.getStoreDefinition().getExchangeConfiguration().getCollaborationToUse().getInitiatorRole());
        RolePerformance supplierRolePerformance = findOrCreateRole(cp.getOwner(), cp.getStoreDefinition().getExchangeConfiguration().getSupplierRole());
        return collaborationService.startCollaboration(cp.getStoreDefinition().getExchangeConfiguration().getCollaborationToUse(), Arrays.asList(requestorRolePerformance, supplierRolePerformance));
    }

    public ReusableBusinessItemAvailability findAvailability(ReusableBusinessItemRequirement requirement) {
        Query q = entityManager.createQuery("select rbip from ReusableBusinessItemPerformance  rbip where rbip.definition.uri = :businessItemDefinitionId and distance(rbip.address.location ,:to) < :maxDistance order by distance(rbip.address.location ,:to) ");
        q.setParameter("businessItemDefinitionId", requirement.getBusinessItemDefinitionId());
        q.setParameter("maxDistance", LocationUtil.meterToEstimatedDegrees(requirement.getMaxDistanceInMeter()));
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        Point location = geometryFactory.createPoint(new Coordinate(requirement.getLongitude(), requirement.getLattitude()));
        q.setParameter("to", location);
        List<ReusableBusinessItemPerformance> matches = q.getResultList();
        SchedulingUtil ssc = new SchedulingUtil();
        List<ScheduleSlot> slots = new ArrayList<ScheduleSlot>();
        for (ReusableBusinessItemPerformance match : matches) {
            slots.addAll(ssc.calculate(match, match.getSchedule(), requirement.getNotBefore(), requirement.getNotAfter()));
        }
        SolverFactory solverFactory = SolverFactory.createFromXmlResource("org/jbpm/vdml/services/impl/scheduling/Booking.xml");
        solverFactory.getSolverConfig().getTerminationConfig().setUnimprovedMillisecondsSpentLimit(10l * slots.size());
        Solver solver = solverFactory.buildSolver();
        BookingSolution booking = new BookingSolution();
        booking.setScheduleSlots(slots);
        Booking problemBooking = new Booking(requirement.getNotBefore(), requirement.getNotAfter(), SchedulingUtil.durationToMillis(requirement.getDuration(), requirement.getDurationTimeUnit()), location);
        booking.setBookings(Arrays.asList(problemBooking));
        booking.setScheduleSlots(slots);
        StopWatch sw = new StopWatch();
        sw.start();
        solver.solve(booking);
        sw.stop();

        BookingSolution solution = (BookingSolution) solver.getBestSolution();
        Booking bestBooking = solution.getBookings().get(0);
        Object scheduledObject = bestBooking.getStartScheduleSlot().getScheduledObject();
        if (bestBooking.getStartScheduleSlot() != null && bestBooking.getEndScheduleSlot() != null) {
            return new ReusableBusinessItemAvailability(((RuntimeEntity) scheduledObject).getId(), bestBooking.getStartScheduleSlot().getFrom(), bestBooking.getEndScheduleSlot().getTo());
        } else {
            return null;
        }
    }

    public CollaborationInstance scheduleReusableProductUse(Long requestorId, ReusableBusinessItemAvailability requiredAvailability) {
        ReusableBusinessItemPerformance reusableResource = entityManager.find(ReusableBusinessItemPerformance.class, requiredAvailability.getReusableBusinessItemId());
        PoolPerformance cp = reusableResource.getHostingPool();
        Participant participant = entityManager.find(Participant.class, requestorId);
        Collaboration collaboration1 = cp.getStoreDefinition().getExchangeConfiguration().getCollaborationToUse();
        RolePerformance requestorRolePerformance = findOrCreateRole(participant, collaboration1.getInitiatorRole());
        RolePerformance supplierRolePerformance = findOrCreateRole(cp.getOwner(), cp.getStoreDefinition().getExchangeConfiguration().getSupplierRole());
        CollaborationInstance observation = collaborationService.startCollaboration(collaboration1, Arrays.asList(requestorRolePerformance, supplierRolePerformance));
        ActivityInstance ao = observation.findActivity(cp.getStoreDefinition().getExchangeConfiguration().getPoolBooking().getActivity());
        final ResourceUseInstance resourceUse = ao.findResourceUse(cp.getStoreDefinition().getExchangeConfiguration().getPoolBooking());
        //TODO check for availability AGAIN here.
        resourceUse.setPlannedFromDateTime(requiredAvailability.getFrom());
        resourceUse.setPlannedToDateTime(requiredAvailability.getTo());
        resourceUse.setQuantity(1);
        resourceUse.setReusableResource(reusableResource);
        resourceUse.setPool(cp);
        PlannedUnavailability pu = new PlannedUnavailability(reusableResource.getSchedule());
        pu.setFrom(resourceUse.getPlannedFromDateTime());
        resourceUse.setPlannedUnavailability(pu);
        switch (resourceUse.getResourceUse().getResourceUseLocation()) {
            case COLLABORATION:
                resourceUse.setAddress(collaboration1.getAddress());
                break;
            case PROVIDING_STORE:
                SupplyingStoreInstance sourcePortContainer = (SupplyingStoreInstance) resourceUse.getInput().getSourcePortContainer();
                resourceUse.setAddress(sourcePortContainer.getStore().getAddress());
                break;
            case RECEIVING_STORE:
                SupplyingStoreInstance targetPortContainer = (SupplyingStoreInstance) resourceUse.getOutput().getTargetPortContainer();
                resourceUse.setAddress(targetPortContainer.getStore().getAddress());
                break;
            case ROLE_PARTICIPANT:
                resourceUse.setAddress(ao.getPerformingRole().getParticipant().getAddress());
                break;
        }
        pu.setAddress(resourceUse.getAddress());
        pu.setTo(resourceUse.getPlannedToDateTime());
        long commitTime = resourceUse.getPlannedFromDateTime().getMillis() - cp.getSchedule().getCommitPeriodTimeUnit().toMillis(cp.getSchedule().getCommitPeriod());
        TimerService ts = TimerServiceRegistry.getInstance().get(collaboration1.getDeploymentId());
        ts.scheduleJob(new CommitScheduledResourceUseJob(), new ScheduledResourceUseJobContext("commit", resourceUse), new PointInTimeTrigger(commitTime, null, null));
        ts.scheduleJob(new FulfillScheduledResourceUseJob(), new ScheduledResourceUseJobContext("fulfill", resourceUse), new PointInTimeTrigger(resourceUse.getPlannedToDateTime().getMillis(), null, null));
        entityManager.flush();
        return observation;
    }


    public void commitToResourceUse(Long resourceUseObservationId) {
        ResourceUseInstance ruo = entityManager.find(ResourceUseInstance.class, resourceUseObservationId);
        if (ruo.getStatus() != ValueFlowStatus.CANCELLED) {
            commitToExchange(ruo.getActivity().getCollaboration().getId());
            ruo.setStatus(ValueFlowStatus.COMMITTED);
            entityManager.flush();
        }
    }

    public CollaborationInstance findExchange(Long id) {
        return entityManager.find(CollaborationInstance.class, id);
    }

    public void cancelBooking(Long id) {
        CollaborationInstance collaborationInstance = findExchange(id);
        DirectlyExchangable exchangeOffering = findExchangeOffering(collaborationInstance);
        Set<ActivityInstance> activities = collaborationInstance.getActivities();
        for (ActivityInstance activity : activities) {
            for (ResourceUseInstance resourceUseInstance : activity.getResourceUseInstance()) {
                if (resourceUseInstance.getPool().equals(exchangeOffering)) {
                    PlannedUnavailability plannedUnavailability = resourceUseInstance.getPlannedUnavailability();
                    if (plannedUnavailability != null) {
                        PersistenceUtil pu = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
                        if (pu.isLoaded(plannedUnavailability.getSchedule(), "plannedUnavailability")) {
                            plannedUnavailability.getSchedule().getPlannedUnavailability().remove(plannedUnavailability);
                        }
                        entityManager.remove(plannedUnavailability);
                        resourceUseInstance.setPlannedUnavailability(null);
                        resourceUseInstance.setQuantity(0);
                        resourceUseInstance.setStatus(ValueFlowStatus.CANCELLED);
                        TimerService ts = TimerServiceRegistry.getInstance().get(collaborationInstance.getCollaboration().getDeploymentId());
                        if (ts instanceof GlobalTimerService) {
                            //Oy weh!
                            JobHandle commitHandle = ((GlobalTimerService) ts).buildJobHandleForContext(new ScheduledResourceUseJobContext("commit", resourceUseInstance));
                            ts.removeJob(commitHandle);
                            JobHandle fulfillHandle = ((GlobalTimerService) ts).buildJobHandleForContext(new ScheduledResourceUseJobContext("fulfill", resourceUseInstance));
                            ts.removeJob(fulfillHandle);
                        }
                    }
                }
            }
        }
        Set<DeliverableFlowInstance> ownedDirectedFlows = collaborationInstance.getOwnedDirectedFlows();
        for (DeliverableFlowInstance directedFlow : ownedDirectedFlows) {
            directedFlow.setStatus(ValueFlowStatus.CANCELLED);
        }
    }

    public void fulfillResourceUse(Long resourceUseObservationId) {
        //TODO not sure we want to automate this
        ResourceUseInstance ruo = entityManager.find(ResourceUseInstance.class, resourceUseObservationId);
        if (ruo.getStatus() != ValueFlowStatus.CANCELLED) {
            fulfillExchange(ruo.getActivity().getCollaboration().getId());
            ruo.setStatus(ValueFlowStatus.FULFILLED);
            entityManager.flush();
        }
    }
}
