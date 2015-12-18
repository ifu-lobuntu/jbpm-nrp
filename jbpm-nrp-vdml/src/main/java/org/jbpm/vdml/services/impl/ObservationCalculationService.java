package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.model.meta.Activity;
import org.jbpm.vdml.services.impl.model.meta.DeliverableFlow;
import org.jbpm.vdml.services.impl.model.meta.PortContainer;
import org.jbpm.vdml.services.impl.model.meta.SupplyingStore;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ObservationCalculationService extends AbstractCalculationService {

    public ObservationCalculationService(EntityManager entityManager) {
        super(entityManager);
    }

    public ObservationCalculationService() {
    }


    public void resolveCollaborationMeasurements(Long collaborationObservationId, ObservationPhase phase) {
        CollaborationInstance collaboration = entityManager.find(CollaborationInstance.class, collaborationObservationId);
        String deploymentId = collaboration.getCollaboration().getDeploymentId();
        //First business items
        for (BusinessItemObservation businessItem : collaboration.getBusinessItems()) {
            Set<? extends Measurement> measurements = businessItem.getMeasurements();
            ObservationContext context = new ObservationContext(phase);
            context.putAll(measurements);
            resolveObservedMeasures(deploymentId, context, measurements);
        }
        //Now follow the activityNetwork
        Set<DeliverableFlow> processedFlows = new HashSet<DeliverableFlow>();
        Set<PortContainer> processedNodes = new HashSet<PortContainer>();
        for (Activity ao : collaboration.getCollaboration().getActivities()) {
            if (ao.getInputFlows().isEmpty()) {
                maybeProcessNode(deploymentId, processedFlows, processedNodes, ao, collaboration, phase);
            }
        }
        for (SupplyingStore ao : collaboration.getCollaboration().getSupplyingStores()) {
            if (ao.getInputFlows().isEmpty()) {
                maybeProcessNode(deploymentId, processedFlows, processedNodes, ao, collaboration, phase);
            }
        }
        for (ValuePropositionInstance vpi : collaboration.getValuePropositions()) {
            if(vpi.isActive()){
                calculateValuePropositionComponents(deploymentId, vpi,phase);
            }
        }
        entityManager.flush();
    }

    protected void calculateValuePropositionComponents(String deploymentId, ValuePropositionInstance vpi, ObservationPhase phase) {
        for (ValuePropositionComponentInstance c : vpi.getComponents()) {
            ObservationContext oc = new ObservationContext(phase);
            for (ValueElementInstance vei : c.getAggregatedFrom()) {
                oc.putAll(vei.getMeasurements());
            }
            oc.putAll(c.getMeasurements());
            super.resolveAllMeasurements(deploymentId, oc);
        }
    }

    private void maybeProcessNode(String deploymentId, Set<DeliverableFlow> processedFlows, Set<PortContainer> processedNodes, PortContainer pc, CollaborationInstance ci, ObservationPhase phase) {
        if (processedFlows.containsAll(pc.getInputFlows()) && !processedNodes.contains(pc)) {
            processedFlows.addAll(pc.getOutputFlows());
            processedNodes.add(pc);
            processAllNodeInstances(deploymentId, pc, ci,phase);
            for (DeliverableFlow flow : pc.getOutputFlows()) {
                if (flow.getTargetPortContainer() instanceof Activity || flow.getTargetPortContainer() instanceof SupplyingStore) {
                    maybeProcessNode(deploymentId, processedFlows, processedNodes, flow.getTargetPortContainer(), ci, phase);
                }
            }
        }
    }

    private void processAllNodeInstances(String deploymentId, PortContainer pc, CollaborationInstance ci, ObservationPhase phase) {
        Collection<? extends PortContainerInstance> portContainers = ci.findPortContainers(pc);
        for (PortContainerInstance pci : portContainers) {
            ObservationContext c = new ObservationContext(phase);
            c.putAll(pci.getMeasurements());
            for (PortInstance portInstance : pci.getContainedPorts()) {
                c.putAll(portInstance.getMeasurements());
                if (portInstance instanceof OutputPortInstance) {
                    OutputPortInstance opi = (OutputPortInstance) portInstance;
                    for (ValueAddInstance valueAddInstance : opi.getValueAdds()) {
                        c.putAll(valueAddInstance.getMeasurements());
                    }
                    for (DeliverableFlowInstance outFlow : opi.getOutflow()) {
                        c.putAll(outFlow.getMeasurements());
                        if (outFlow.getDeliverable() != null) {
                            c.putAll(outFlow.getDeliverable().getMeasurements());
                        }
                    }
                } else {
                    InputPortInstance ipi = (InputPortInstance) portInstance;
                    for (DeliverableFlowInstance inFlow : ipi.getInflow()) {
                        c.putAll(inFlow.getMeasurements());
                        c.putAll(inFlow.getSource().getMeasurements());
                        if (inFlow.getDeliverable() != null) {
                            c.putAll(inFlow.getDeliverable().getMeasurements());
                        }
                    }
                }
            }
            if (pci instanceof ActivityInstance) {
                Set<ResourceUseInstance> resourceUseInstance = ((ActivityInstance) pci).getResourceUseInstance();
                for (ResourceUseInstance rui : resourceUseInstance) {
                    c.putAll(rui.getMeasurements());
                }
            }
            resolveAllMeasurements(deploymentId, c);
        }
    }

}


