package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObservationCalculationService extends AbstractCalculationService {

    public ObservationCalculationService(EntityManager entityManager) {
        super(entityManager);
    }

    public ObservationCalculationService() {
    }


    public void resolveCollaborationMeasurements(Long collaborationObservationId) {
        CollaborationInstance collaboration = entityManager.find(CollaborationInstance.class, collaborationObservationId);
        String deploymentId = collaboration.getCollaboration().getDeploymentId();
        //First business items
        for (BusinessItemObservation businessItem : collaboration.getBusinessItems()) {
            Set<? extends Measurement> measurements = businessItem.getMeasurements();
            Map<String, Measurement> context = new HashMap<String, Measurement>();
            addToContext(context, measurements);
            resolveMeasurements(deploymentId, context, measurements);
        }
        //Now follow the activityNetwork
        Set<DeliverableFlowInstance> processedFlows = new HashSet<DeliverableFlowInstance>();
        Set<PortContainerInstance> processedActivities = new HashSet<PortContainerInstance>();
        for (ActivityInstance ao : collaboration.getActivities()) {
            maybeProcessActivity(deploymentId, processedFlows, processedActivities, ao);
        }
        for (SupplyingStoreInstance ao : collaboration.getSupplyingStores()) {
            maybeProcessActivity(deploymentId, processedFlows, processedActivities, ao);
        }
        entityManager.flush();
    }

    private void maybeProcessActivity(String deploymentId, Set<DeliverableFlowInstance> processedFlows, Set<PortContainerInstance> processedActivities, PortContainerInstance ao) {
        if (processedFlows.containsAll(ao.getConcludedFlow()) && !processedActivities.contains(ao)) {
            processedActivities.add(ao);
            Map<String, Measurement> context = new HashMap<String, Measurement>();
            for (DeliverableFlowInstance flow : ao.getConcludedFlow()) {
                addToContext(context, flow.getMeasurements());
                addToContext(context, flow.getValueAddMeasurements());
            }
            if (ao instanceof ActivityInstance) {
                for (ResourceUseInstance ruo : ((ActivityInstance) ao).getResourceUseInstance()) {
                    addToContext(context, ruo.getMeasurements());
                    resolveMeasurements(deploymentId, context, ruo.getMeasurements());
                }
            }
            addToContext(context, ao.getMeasurements());
            resolveMeasurements(deploymentId, context, ao.getMeasurements());
            for (DeliverableFlowInstance flow : ao.getCommencedFlow()) {
                if (flow.getQuantity() != null && flow.getQuantity().getMeasure().getName().equals("Profit")) {
                    System.out.println();
                }
                addToContext(context, flow.getMeasurements());
                addToContext(context, flow.getValueAddMeasurements());
                resolveMeasurements(deploymentId, context, flow.getMeasurements());
                resolveMeasurements(deploymentId, context, flow.getValueAddMeasurements());
                processedFlows.add(flow);
                if (flow.getTargetPortContainer() instanceof ActivityInstance || flow.getTargetPortContainer() instanceof SupplyingStoreInstance) {
                    for (DeliverableFlowInstance peerFlow : flow.getTargetPortContainer().getConcludedFlow()) {
                        addToContext(context, peerFlow.getDeliverable().getMeasurements());
                        addToContext(context, peerFlow.getMeasurements());
                    }
                    for (DeliverableFlowInstance peerFlow : flow.getTargetPortContainer().getConcludedFlow()) {
                        //Recalculate inputs as this is the first time they will be computed in one context
                        resolveMeasurements(deploymentId, context, peerFlow.getMeasurements());
                    }
                    resolveMeasurements(deploymentId, context, flow.getMeasurements());
                    maybeProcessActivity(deploymentId, processedFlows, processedActivities, flow.getTargetPortContainer());
                }
            }
            resolveMeasurements(deploymentId, context, ao.getMeasurements());//To process ValueAdds used
        }
    }
}
