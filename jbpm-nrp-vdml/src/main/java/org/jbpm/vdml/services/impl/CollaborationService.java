package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.joda.time.DateTime;


import javax.persistence.EntityManager;
import java.util.*;

/**
 * 9. As a project custodian, when planning on a project has been completed, I want all collaborating participants, including myself, to commit to the times, locations and quantities so that subsequent planning can be based on accurate data.
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

    public CollaborationInstance startCollaboration(Collaboration collaboration, Collection<RolePerformance> rolePerformances) {
        CollaborationInstance observation = new CollaborationInstance(collaboration);
        entityManager.persist(observation);
        syncRuntimeEntities(observation.getMilestones(), collaboration.getMilestones(), MilestoneInstance.class, observation);
        syncRuntimeEntities(observation.getActivities(), collaboration.getActivities(), ActivityInstance.class, observation);
        syncRuntimeEntities(observation.getSupplyingStores(), collaboration.getSupplyingStores(), SupplyingStoreInstance.class, observation);
        syncRuntimeEntities(observation.getBusinessItems(), collaboration.getBusinessItemDefinitions(), BusinessItemObservation.class, observation);
        for (BusinessItemObservation bio : observation.getBusinessItems()) {
            syncRuntimeEntities(bio.getMeasurements(), bio.getDefinition().getImmediateMeasures(), BusinessItemMeasurement.class, bio);
        }
        syncRuntimeEntities(observation.getOwnedDirectedFlows(), collaboration.getFlows(), DeliverableFlowInstance.class, observation);
        for (DeliverableFlowInstance dfo : observation.getOwnedDirectedFlows()) {
            Collection<Measure> measures = new HashSet<Measure>(dfo.getDeliverableFlow().getMeasures());
            if(dfo.getDeliverableFlow().getQuantity()!=null){
                measures.add(dfo.getDeliverableFlow().getQuantity());
            }
            if(dfo.getDeliverableFlow().getDuration()!=null){
                measures.add(dfo.getDeliverableFlow().getDuration());
            }
            syncRuntimeEntities(dfo.getMeasurements(), measures, DeliverableFlowMeasurement.class, dfo);
            if(dfo.getDeliverableFlow().getSource() instanceof OutputPort){
                syncRuntimeEntities(dfo.getValueAddMeasurements(), dfo.getDeliverableFlow().getSource().getValueAdds(), ValueAddMeasurement.class, dfo);
            }
            if(dfo.getDeliverableFlow() instanceof DeliverableFlow){
                Milestone milestone = dfo.getDeliverableFlow().getMilestone();
                if(milestone!=null){
                    dfo.setMilestone(observation.findMilestone(milestone));
                }
            }
        }
        for (ActivityInstance ao : observation.getActivities()) {
            syncRuntimeEntities(ao.getMeasurements(), ao.getActivity().getMeasures(), ActivityMeasurement.class, ao);
            Collection<ResourceUseInstance> ruos = syncRuntimeEntities(ao.getResourceUseInstance(), ao.getActivity().getResourceUses(), ResourceUseInstance.class, ao);
            for (ResourceUseInstance ruo : ruos) {
                syncRuntimeEntities(ruo.getMeasurements(), asCollection(ruo.getResourceUse().getDuration(), ruo.getResourceUse().getQuantity()),ResourceUseMeasurement.class, ruo);
            }
        }
        for (SupplyingStoreInstance so : observation.getSupplyingStores()) {
            syncRuntimeEntities(so.getMeasurements(),so.getSupplyingStore().getMeasures(),SupplyingStoreMeasurement.class,so);
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

    /**
     * To be used when we support multiple instances of the same activity
     * @param activityId
     * @param collaborationObservationId
     * @param capabilityPerformance
     * @return
     */

    public ActivityInstance newActivity(String activityId, Long collaborationObservationId, Long capabilityPerformance) {
        CollaborationInstance co = entityManager.find(CollaborationInstance.class, collaborationObservationId);
        CapabilityOffer cp = entityManager.find(CapabilityOffer.class, capabilityPerformance);
        Activity a = entityManager.find(Activity.class, activityId);
        ActivityInstance ao = new ActivityInstance(a, co);
        entityManager.persist(ao);
        throw new RuntimeException();
//        for (DirectedFlow flow : a.getConcludedFlows()) {
//            new DeliverableFlowInstance(flow, co, co.findPortContainer(flow.getSourcePortContainer()), ao);
//        }
//        for (DirectedFlow flow : a.getCommencedFlows()) {
//            new DeliverableFlowInstance(flow, co, ao, co.findPortContainer(flow.getTargetPortContainer()));
//        }
//        entityManager.flush();
//        return ao;
    }

    public void commitToActivity(Long collaborationId, String activityName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationInstance collaborationInstance = entityManager.find(CollaborationInstance.class, collaborationId);
        ActivityInstance ao = collaborationInstance.findActivity(collaborationInstance.getCollaboration().findActivity(activityName));
        commitToFlows(ao.getConcludedFlow(), ao.getCommencedFlow());
    }
    public void fulfillActivity(Long collaborationId, String activityName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationInstance collaborationInstance = entityManager.find(CollaborationInstance.class, collaborationId);
        ActivityInstance ao = collaborationInstance.findActivity(collaborationInstance.getCollaboration().findActivity(activityName));
        fulfillFlows(ao.getConcludedFlow(), ao.getCommencedFlow());
    }

    public void commitToMilestone(MilestoneInstance mo) {
        mo.commit();
        commitToFlows(mo.getFlows());
        entityManager.flush();
    }

    public void fulfillMilestone(MilestoneInstance mo) {
        mo.occur();
        fulfillFlows(mo.getFlows());
        entityManager.flush();
    }

    protected void commitToFlows(Set<? extends DeliverableFlowInstance>... flows) {
        for (Set<? extends DeliverableFlowInstance> flowSet : flows) {
            for (DeliverableFlowInstance flow : flowSet) {
                if (flow.getSourcePortContainer() instanceof SupplyingStoreInstance) {
                    SupplyingStoreInstance rsp = (SupplyingStoreInstance) flow.getSourcePortContainer();
                    if (flow.getQuantity() != null) {
                        flow.setStatus(ValueFlowStatus.COMMITTED);
                        Measurement q = flow.getQuantity();
                        if (q.getActualValue() != null) {
                            rsp.getStore().setProjectedInventoryLevel(rsp.getStore().getProjectedInventoryLevel() - q.getActualValue());
                        }
                    }
                }
                if (flow.getTargetPortContainer() instanceof SupplyingStoreInstance) {
                    SupplyingStoreInstance rsp = (SupplyingStoreInstance) flow.getTargetPortContainer();
                    if (flow.getQuantity() != null) {
                        Measurement q = flow.getQuantity();
                        flow.setStatus(ValueFlowStatus.COMMITTED);
                        if (q.getActualValue() != null) {
                            rsp.getStore().setProjectedInventoryLevel(rsp.getStore().getProjectedInventoryLevel() + q.getActualValue());
                        }
                    }
                }
            }
        }
    }


    protected void fulfillFlows(Set<? extends DeliverableFlowInstance>... flowsArray) {
        for (Set<? extends DeliverableFlowInstance> flows : flowsArray) {
            for (DeliverableFlowInstance flow : flows) {
                if (flow.getSourcePortContainer() instanceof SupplyingStoreInstance) {
                    SupplyingStoreInstance rsp = (SupplyingStoreInstance) flow.getSourcePortContainer();
                    if (flow.getQuantity() != null) {
                        flow.setStatus(ValueFlowStatus.FULFILLED);
                        flow.setActualDate(new DateTime());
                        Measurement q = flow.getQuantity();
                        if (q.getActualValue() != null) {
                            rsp.getStore().setInventoryLevel(rsp.getStore().getInventoryLevel() - q.getActualValue());
                        }
                    }
                }
                if (flow.getTargetPortContainer() instanceof SupplyingStoreInstance) {
                    SupplyingStoreInstance rsp = (SupplyingStoreInstance) flow.getTargetPortContainer();
                    if (flow.getQuantity() != null) {
                        Measurement q = flow.getQuantity();
                        flow.setStatus(ValueFlowStatus.FULFILLED);
                        flow.setActualDate(new DateTime());
                        if (q.getActualValue() != null) {
                            rsp.getStore().setInventoryLevel(rsp.getStore().getInventoryLevel() + q.getActualValue());
                        }
                    }
                }
            }
        }
    }
}
