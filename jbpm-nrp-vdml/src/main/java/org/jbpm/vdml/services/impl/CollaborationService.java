package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.api.model.LinkedExternalObject;
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

    public CollaborationInstance startCollaboration(CapabilityMethod collaboration, Collection<RolePerformance> rolePerformances) {
        CollaborationInstance observation = new CollaborationInstance(collaboration);
        entityManager.persist(observation);
        syncRuntimeEntities(observation.getMilestones(), collaboration.getMilestones(), MilestoneInstance.class, observation);
        syncPorts(observation, collaboration);
        Collection<ActivityInstance> activityInstances = syncRuntimeEntities(observation.getActivities(), collaboration.getActivities(), ActivityInstance.class, observation);
        for (ActivityInstance ai : activityInstances) {
            syncPorts(ai, ai.getActivity());
        }
        Collection<SupplyingStoreInstance> ssi = syncRuntimeEntities(observation.getSupplyingStores(), collaboration.getSupplyingStores(), SupplyingStoreInstance.class, observation);
        for (SupplyingStoreInstance storeInstance : ssi) {
            syncPorts(storeInstance, storeInstance.getSupplyingStore());
        }
        syncRuntimeEntities(observation.getBusinessItems(), collaboration.getBusinessItemDefinitions(), BusinessItemObservation.class, observation);
        for (BusinessItemObservation bio : observation.getBusinessItems()) {
            syncRuntimeEntities(bio.getMeasurements(), bio.getDefinition().getImmediateMeasures(), BusinessItemMeasurement.class, bio);
        }
        syncRuntimeEntities(observation.getOwnedDirectedFlows(), collaboration.getFlows(), DeliverableFlowInstance.class, observation);
        for (DeliverableFlowInstance dfo : observation.getOwnedDirectedFlows()) {
            DeliverableFlow df = dfo.getDeliverableFlow();
            syncRuntimeEntities(dfo.getMeasurements(), df.getMeasures(), DeliverableFlowMeasurement.class, dfo, dfo.getDeliverableFlow().getDuration());
            Milestone milestone = dfo.getDeliverableFlow().getMilestone();
            if (milestone != null) {
                dfo.setMilestone(observation.findMilestone(milestone));
            }
            if (df.getDeliverable() != null) {
                //At this point, there will be only one
                dfo.setDeliverable(observation.findFirstBusinessItem(df.getDeliverable()));
            }
        }
        for (ActivityInstance ao : observation.getActivities()) {
            syncRuntimeEntities(ao.getMeasurements(), ao.getActivity().getMeasures(), ActivityMeasurement.class, ao);
            syncResourceUses(ao);
        }
        for (SupplyingStoreInstance so : observation.getSupplyingStores()) {
            syncRuntimeEntities(so.getMeasurements(), so.getSupplyingStore().getMeasures(), SupplyingStoreMeasurement.class, so);
        }
        assignmentService.assignToRoles(observation, rolePerformances);
        return observation;
    }

    protected void syncResourceUses(ActivityInstance ao) {
        Collection<ResourceUseInstance> ruos = syncRuntimeEntities(ao.getResourceUseInstance(), ao.getActivity().getResourceUses(), ResourceUseInstance.class, ao);
        for (ResourceUseInstance ruo : ruos) {
            ruo.setInput(ao.findInputPort(ruo.getResourceUse().getInput()));
            ruo.setOutput(ao.findOutputPort(ruo.getResourceUse().getOutput()));
            syncRuntimeEntities(ruo.getMeasurements(), asCollection(ruo.getResourceUse().getDuration(), ruo.getResourceUse().getQuantity()), ResourceUseMeasurement.class, ruo);
        }
    }

    protected void syncPorts(PortContainerInstance ai, PortContainer pc) {
        for (Port port : pc.getContainedPorts()) {
            if (port instanceof OutputPort) {
                OutputPortInstance opi = new OutputPortInstance((OutputPort) port, ai);
                syncRuntimeEntities(opi.getMeasurements(), port.getMeasures(), PortMeasurement.class, opi, port.getBatchSize());
                syncRuntimeEntities(opi.getValueAdds(), ((OutputPort) port).getValueAdds(), ValueAddInstance.class, opi);
                for (ValueAddInstance valueAddInstance : opi.getValueAdds()) {
                    syncRuntimeEntities(valueAddInstance.getMeasurements(),valueAddInstance.getValueAdd().getMeasures(), ValueAddInstanceMeasurement.class,valueAddInstance, valueAddInstance.getValueAdd().getValueMeasure());
                }
            } else {
                InputPortInstance opi = new InputPortInstance((InputPort) port, ai);
                syncRuntimeEntities(opi.getMeasurements(), port.getMeasures(), PortMeasurement.class, opi, port.getBatchSize());
            }
        }
    }

    public static <T> Collection<? extends T> asCollection(T... o) {
        Set<T> result = new HashSet<T>();
        for (T t : o) {
            if (t != null) {
                result.add(t);
            }
        }

        return result;
    }

    /**
     * To be used when we support multiple instances of the same activity. Currently this will only happen when a new instance of the key input
     * to the activity is created/made available
     */

    public ActivityInstance newActivity(CollaborationInstance co, Activity a, String inputName, LinkedExternalObject inputValue) {
        InputPort inputPort = a.findInputPort(inputName);
        DeliverableFlow inputDf = inputPort.getInput();
        if (inputDf == null && inputDf.getDeliverable() == null) {
            throw new IllegalArgumentException();
        }
        ActivityInstance ao = new ActivityInstance(a, co);
        entityManager.persist(ao);
        syncPorts(ao, a);
        syncRuntimeEntities(ao.getMeasurements(), a.getMeasures(), ActivityMeasurement.class, ao);
        for (PortInstance port : ao.getContainedPorts()) {
            if (port.getPort().equals(inputPort)) {
                createInitiatingInput(inputValue, ao,(InputPortInstance) port);
            } else if (port instanceof InputPortInstance) {
                createNonInitiatingInput(co, (InputPortInstance) port);
            } else {
                createOutput(co, (OutputPortInstance) port);
            }

        }
        syncResourceUses(ao);

        entityManager.flush();
        return ao;
    }

    private void createOutput(CollaborationInstance co, OutputPortInstance opi) {
        BusinessItemObservation bio = new BusinessItemObservation(opi.getPort().getOutput().getDeliverable(), co);
        entityManager.persist(bio);
        syncRuntimeEntities(bio.getMeasurements(), bio.getDefinition().getMeasures(), BusinessItemMeasurement.class, bio);
        DeliverableFlowInstance dfi = new DeliverableFlowInstance(co, opi,bio);
        entityManager.persist(dfi);
        syncRuntimeEntities(dfi.getMeasurements(), dfi.getDeliverableFlow().getMeasures(), DeliverableFlowMeasurement.class, dfi, dfi.getDeliverableFlow().getDuration());
    }

    private void createNonInitiatingInput(CollaborationInstance co, InputPortInstance ipi) {
        DeliverableFlowInstance dfi = new DeliverableFlowInstance(co, ipi);
        entityManager.persist(dfi);
        syncRuntimeEntities(dfi.getMeasurements(), dfi.getDeliverableFlow().getMeasures(), DeliverableFlowMeasurement.class, dfi, dfi.getDeliverableFlow().getDuration());
    }

    private void createInitiatingInput(LinkedExternalObject inputValue, ActivityInstance ao, InputPortInstance inputPortInstance) {
        BusinessItemObservation bio = new BusinessItemObservation(inputPortInstance.getPort().getInput().getDeliverable(), ao.getCollaboration());
        bio.setLocalReference(new ExternalObjectReference(inputValue.getObjectType(), inputValue.getIdentifier()));
        entityManager.persist(bio);
        syncRuntimeEntities(bio.getMeasurements(), bio.getDefinition().getMeasures(), BusinessItemMeasurement.class, bio);
        DeliverableFlowInstance dfi = new DeliverableFlowInstance(ao.getCollaboration(), bio, inputPortInstance);
        entityManager.persist(dfi);
        syncRuntimeEntities(dfi.getMeasurements(), dfi.getDeliverableFlow().getMeasures(), DeliverableFlowMeasurement.class, dfi, dfi.getDeliverableFlow().getDuration());
    }

    public void commitToActivity(Long collaborationId, String activityName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationInstance collaborationInstance = entityManager.find(CollaborationInstance.class, collaborationId);
        ActivityInstance ao = collaborationInstance.findFirstActivity(collaborationInstance.getCollaboration().findActivity(activityName));
        commitToFlows(ao.getConcludedFlow(), ao.getCommencedFlow());
    }

    public void fulfillActivity(Long collaborationId, String activityName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationInstance collaborationInstance = entityManager.find(CollaborationInstance.class, collaborationId);
        ActivityInstance ao = collaborationInstance.findFirstActivity(collaborationInstance.getCollaboration().findActivity(activityName));
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
