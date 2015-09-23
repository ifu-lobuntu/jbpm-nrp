package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.*;


import javax.persistence.EntityManager;
import java.util.*;

/**
 * 9. As a project custodian, when planning on a project has been completed, I want all collaborating participants, including myself, to commit to the times, locations and quantitiesm so that subsequent planning can be based on accurate data.
 * 10. As a project custodian, when a given milestone has been reached, I want relevant flow of value to be effected so that the inventory levels on the stores and pools used reflect the current state of affairs accurately.
 */
public class CollaborationService extends AbstractRuntimeService {
    AssignmentService assignmentService;

    public CollaborationService() {

    }

    public CollaborationService(EntityManager entityManager) {
        super(entityManager);
        this.assignmentService = new AssignmentService(entityManager);
    }

    public CollaborationObservation startCollaboration(Collaboration collaboration, Collection<RolePerformance> rolePerformances) {
        CollaborationObservation observation = new CollaborationObservation(collaboration);
        entityManager.persist(observation);
        syncRuntimeEntities(observation.getMilestones(), collaboration.getMilestones(), MilestoneObservation.class, observation);
        syncRuntimeEntities(observation.getActivities(), collaboration.getActivities(), ActivityObservation.class, observation);
        syncRuntimeEntities(observation.getSupplyingStores(), collaboration.getSupplyingStores(), SupplyingStoreObservation.class, observation);
        syncRuntimeEntities(observation.getBusinessItems(), collaboration.getBusinessItemDefinitions(), BusinessItemObservation.class, observation);
        for (BusinessItemObservation bio : observation.getBusinessItems()) {
            syncRuntimeEntities(bio.getMeasurements(), bio.getBusinessItemDefinition().getImmediateMeasures(), BusinessItemMeasurement.class, bio);
        }
        syncRuntimeEntities(observation.getOwnedDirectedFlows(), collaboration.getFlows(), DirectedFlowObservation.class, observation);
        for (DirectedFlowObservation dfo : observation.getOwnedDirectedFlows()) {
            Collection<Measure> measures = new HashSet<Measure>(dfo.getDirectedFlow().getMeasures());
            if(dfo.getDirectedFlow().getQuantity()!=null){
                measures.add(dfo.getDirectedFlow().getQuantity());
            }
            if(dfo.getDirectedFlow().getDuration()!=null){
                measures.add(dfo.getDirectedFlow().getDuration());
            }
            syncRuntimeEntities(dfo.getMeasurements(), measures, DirectedFlowMeasurement.class, dfo);
            if(dfo.getDirectedFlow() instanceof DeliverableFlow){
                Milestone milestone = ((DeliverableFlow) dfo.getDirectedFlow()).getMilestone();
                if(milestone!=null){
                    dfo.setMilestone(observation.findMilestone(milestone));
                }
            }
        }
        for (ActivityObservation ao : observation.getActivities()) {
            Collection<ResourceUseObservation> ruos = syncRuntimeEntities(ao.getResourceUseObservation(), ao.getActivity().getResourceUses(), ResourceUseObservation.class, ao);
            for (ResourceUseObservation ruo : ruos) {
                syncRuntimeEntities(ruo.getMeasurements(), asCollection(ruo.getResourceUse().getDuration(), ruo.getResourceUse().getQuantity()),ResourceUseMeasurement.class, ruo);
            }
        }
        assignmentService.assignToRoles(observation, rolePerformances);
        return observation;
    }
    public static <T> Collection <? extends T> asCollection(T ...o){
        Set<T> result = new HashSet<T>();
        for (T t : o) {
            if(t!=null){
                result.add(t);
            }
        }

        return result;
    }

    public ActivityObservation newActivity(String activityId, Long collaborationObservationId, Long capabilityPerformance) {
        CollaborationObservation co = entityManager.find(CollaborationObservation.class, collaborationObservationId);
        CapabilityPerformance cp = entityManager.find(CapabilityPerformance.class, capabilityPerformance);
        Activity a = entityManager.find(Activity.class, activityId);
        ActivityObservation ao = new ActivityObservation(a, co);
        entityManager.persist(ao);
        for (DirectedFlow flow : a.getConcludedFlows()) {
            new DirectedFlowObservation(flow, co, co.findPortContainer(flow.getSourcePortContainer()), ao);
        }
        for (DirectedFlow flow : a.getCommencedFlows()) {
            new DirectedFlowObservation(flow, co, ao, co.findPortContainer(flow.getTargetPortContainer()));
        }
        entityManager.flush();
        return ao;
    }

    public void commitToActivity(Long collaborationId, String activityName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationObservation collaborationObservation = entityManager.find(CollaborationObservation.class, collaborationId);
        ActivityObservation ao = collaborationObservation.findActivity(collaborationObservation.getCollaboration().findActivity(activityName));
        commitToFlows(ao.getConcludedFlow(), ao.getCommencedFlow());
    }
    public void fulfillActivity(Long collaborationId, String activityName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationObservation collaborationObservation = entityManager.find(CollaborationObservation.class, collaborationId);
        ActivityObservation ao = collaborationObservation.findActivity(collaborationObservation.getCollaboration().findActivity(activityName));
        fulfillFlows(ao.getConcludedFlow(), ao.getCommencedFlow());
    }

    public void commitToMilestone(MilestoneObservation mo) {
        mo.commit();
        commitToFlows(mo.getFlows());
        entityManager.flush();
    }

    public void fulfillMilestone(MilestoneObservation mo) {
        mo.occur();
        fulfillFlows(mo.getFlows());
        entityManager.flush();
    }

    protected void commitToFlows(Set<? extends DirectedFlowObservation>... flows) {
        for (Set<? extends DirectedFlowObservation> flowSet : flows) {
            for (DirectedFlowObservation flow : flowSet) {
                if (flow.getSourcePortContainer() instanceof SupplyingStoreObservation) {
                    SupplyingStoreObservation rsp = (SupplyingStoreObservation) flow.getSourcePortContainer();
                    if (flow.getQuantity() != null) {
                        flow.setStatus(ValueFlowStatus.COMMITTED);
                        Measurement q = flow.getQuantity();
                        if (q.getValue() != null) {
                            rsp.getStore().setProjectedInventoryLevel(rsp.getStore().getProjectedInventoryLevel() - q.getValue());
                        }
                    }
                }
                if (flow.getTargetPortContainer() instanceof SupplyingStoreObservation) {
                    SupplyingStoreObservation rsp = (SupplyingStoreObservation) flow.getTargetPortContainer();
                    if (flow.getQuantity() != null) {
                        Measurement q = flow.getQuantity();
                        flow.setStatus(ValueFlowStatus.COMMITTED);
                        if (q.getValue() != null) {
                            rsp.getStore().setProjectedInventoryLevel(rsp.getStore().getProjectedInventoryLevel() + q.getValue());
                        }
                    }
                }
            }
        }
    }


    protected void fulfillFlows(Set<? extends DirectedFlowObservation>... flowsArray) {
        for (Set<? extends DirectedFlowObservation> flows : flowsArray) {
            for (DirectedFlowObservation flow : flows) {
                if (flow.getSourcePortContainer() instanceof SupplyingStoreObservation) {
                    SupplyingStoreObservation rsp = (SupplyingStoreObservation) flow.getSourcePortContainer();
                    if (flow.getQuantity() != null) {
                        flow.setStatus(ValueFlowStatus.FULFILLED);
                        flow.setActualDate(new Date());
                        Measurement q = flow.getQuantity();
                        if (q.getValue() != null) {
                            rsp.getStore().setInventoryLevel(rsp.getStore().getInventoryLevel() - q.getValue());
                        }
                    }
                }
                if (flow.getTargetPortContainer() instanceof SupplyingStoreObservation) {
                    SupplyingStoreObservation rsp = (SupplyingStoreObservation) flow.getTargetPortContainer();
                    if (flow.getQuantity() != null) {
                        Measurement q = flow.getQuantity();
                        flow.setStatus(ValueFlowStatus.FULFILLED);
                        flow.setActualDate(new Date());
                        if (q.getValue() != null) {
                            rsp.getStore().setInventoryLevel(rsp.getStore().getInventoryLevel() + q.getValue());
                        }
                    }
                }
            }
        }
    }
}
