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
        CollaborationObservation collaboration = entityManager.find(CollaborationObservation.class, collaborationObservationId);
        String deploymentId = collaboration.getCollaboration().getDeploymentId();
        //First business items
        for (BusinessItemObservation businessItem : collaboration.getBusinessItems()) {
            Set<? extends Measurement> measurements = businessItem.getMeasurements();
            Map<String, Measurement> context = new HashMap<String, Measurement>();
            addToContext(context, measurements);
            resolveMeasurements(deploymentId, context, measurements);
        }
        //Now follow the activityNetwork
        Set<DirectedFlowObservation> processedFlows = new HashSet<DirectedFlowObservation>();
        Set<PortContainerObservation> processedActivities = new HashSet<PortContainerObservation>();
        for (ActivityObservation ao : collaboration.getActivities()) {
            maybeProcessActivity(deploymentId, processedFlows, processedActivities, ao);
        }
        for (SupplyingStoreObservation ao : collaboration.getSupplyingStores()) {
            maybeProcessActivity(deploymentId, processedFlows, processedActivities, ao);
        }
        entityManager.flush();
    }

    private void maybeProcessActivity(String deploymentId, Set<DirectedFlowObservation> processedFlows, Set<PortContainerObservation> processedActivities, PortContainerObservation ao) {
        if (processedFlows.containsAll(ao.getConcludedFlow()) && !processedActivities.contains(ao)) {
            processedActivities.add(ao);
            Map<String, Measurement> context = new HashMap<String, Measurement>();
            for (DirectedFlowObservation flow : ao.getConcludedFlow()) {
                addToContext(context, flow.getMeasurements());
                addToContext(context, flow.getValueAddMeasurements());
            }
            if (ao instanceof ActivityObservation) {
                for (ResourceUseObservation ruo : ((ActivityObservation) ao).getResourceUseObservation()) {
                    addToContext(context, ruo.getMeasurements());
                    resolveMeasurements(deploymentId, context, ruo.getMeasurements());
                }
            }
            addToContext(context, ao.getMeasurements());
            resolveMeasurements(deploymentId, context, ao.getMeasurements());
            for (DirectedFlowObservation flow : ao.getCommencedFlow()) {
                if (flow.getQuantity() != null && flow.getQuantity().getMeasure().getName().equals("Profit")) {
                    System.out.println();
                }
                addToContext(context, flow.getMeasurements());
                addToContext(context, flow.getValueAddMeasurements());
                resolveMeasurements(deploymentId, context, flow.getMeasurements());
                resolveMeasurements(deploymentId, context, flow.getValueAddMeasurements());
                processedFlows.add(flow);
                if (flow.getTargetPortContainer() instanceof ActivityObservation || flow.getTargetPortContainer() instanceof SupplyingStoreObservation) {
                    for (DirectedFlowObservation peerFlow : flow.getTargetPortContainer().getConcludedFlow()) {
                        addToContext(context, peerFlow.getDeliverable().getMeasurements());
                        addToContext(context, peerFlow.getMeasurements());
                    }
                    for (DirectedFlowObservation peerFlow : flow.getTargetPortContainer().getConcludedFlow()) {
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
