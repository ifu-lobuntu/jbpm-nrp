package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

public class ValueCalculationService {
    private EntityManager entityManager;

    public ValueCalculationService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ValueCalculationService() {
    }

    public void calculateProvidedValueProposition(ProvidedValuePropositionPerformance vpf) {
        //similar to calculateValueProposition without the receiver as criterion
    }

    public void doCollaborationObservations(Long collaborationObservationId) {
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
                if(flow.getQuantity()!=null && flow.getQuantity().getMeasure().getName().equals("Profit")){
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


    private void resolveMeasurements(String deploymentId, Map<String, Measurement> context, Collection<? extends Measurement> measurements) {
        for (Measurement measurement : measurements) {
            resolveMeasurement(deploymentId, context, measurement);
        }
    }

    private void addToContext(Map<String, Measurement> context, Collection<? extends Measurement> measurements) {
        for (Measurement measurement : measurements) {
            context.put(measurement.getMeasure().getUri(), measurement);
        }
    }

    public void calculateCapabilityPerformance(CapabilityPerformance cp) {
        Map<String, Measurement> measurements = new HashMap<String, Measurement>();
        Set<Measurement> otherMeasurements = new HashSet<Measurement>();
        for (CapabilityMeasurement measurement : cp.getMeasurements()) {
            measurements.put(measurement.getMeasure().getUri(), measurement);
            String additionalCriteria = "and m.activity.responsibleRole.participant= :performer";
            if (measurement.getMeasure() instanceof CollectiveMeasure) {
                CollectiveMeasure cm = (CollectiveMeasure) measurement.getMeasure();
                Accumulator accumulator = cm.getAccumulator();
                Query q = entityManager.createQuery("select " + accumulator.name() + "(m.value) from ActivityMeasurement m where m.measure.uri in :measureUri " + additionalCriteria);
                Set<String> uris = new HashSet<String>();
                for (EmfReference emfReference : cm.getAggregatedMeasures()) {
                    uris.add(emfReference.getUri());
                }
                q.setParameter("measureUri", uris);
                q.setParameter("performer", cp.getParticipant());
                measurement.setActualValue(extractDouble(q));
            } else if (measurement.getMeasure() instanceof CountingMeasure) {
                CountingMeasure cm = (CountingMeasure) measurement.getMeasure();
                Query q = entityManager.createQuery("select count(m) from ActivityMeasurement m where m.measure.uri =:measureUri and m.value is not null " + additionalCriteria + " and " + cm.getValuesToCount());
                q.setParameter("measureUri", cm.getMeasureToCount().getUri());
                q.setParameter("performer", cp.getParticipant());
                measurement.setActualValue(extractDouble(q));
            } else {
                otherMeasurements.add(measurement);
            }
        }
        resolveMeasurements(cp.getCapability().getDeploymentId(), measurements, otherMeasurements);
    }

    public void calculateStorePerformance(SupplyingStoreObservation rsp) {
        Map<String, Measurement> measurements = new HashMap<String, Measurement>();
        Set<Measurement> otherMeasurements = new HashSet<Measurement>();
        for (SupplyingStoreMeasurement measurement : rsp.getMeasurements()) {
            measurements.put(measurement.getMeasure().getUri(), measurement);
            String additionalCriteria = "and m.activity.responsibleRole.participant= :owner and m.deliverableFlow.toActivity.performer= :receiver";
            if (measurement.getMeasure() instanceof CollectiveMeasure) {
                CollectiveMeasure cm = (CollectiveMeasure) measurement.getMeasure();
                Accumulator accumulator = cm.getAccumulator();
                Query q = entityManager.createQuery("select " + accumulator.name() + "(m.value) from DirectedFlowObservation d inner join d.deliverable.businessItem m where d.sourcePortContainer = :store and m.measure.uri in :measureUri " + additionalCriteria);
                Set<String> uris = new HashSet<String>();
                for (EmfReference emfReference : cm.getAggregatedMeasures()) {
                    uris.add(emfReference.getUri());
                }
                q.setParameter("measureUri", uris);
                q.setParameter("store", rsp);
                measurement.setActualValue(extractDouble(q));
            } else if (measurement.getMeasure() instanceof CountingMeasure) {
                CountingMeasure cm = (CountingMeasure) measurement.getMeasure();
                Query q = entityManager.createQuery("select count(m) from DirectedFlowObservation d inner join d.deliverable.businessItem where d.sourcePortContainer = :store and m.measure.uri in :measureUri and m.value is not null " + additionalCriteria + " and " + cm.getValuesToCount());
                q.setParameter("measureUri", cm.getMeasureToCount().getUri());
                q.setParameter("store", rsp);
                measurement.setActualValue(extractDouble(q));
            } else {
                otherMeasurements.add(measurement);
            }
        }
        resolveMeasurements(rsp.getStore().getDeploymentId(), measurements, otherMeasurements);
    }

    private Double extractDouble(Query q) {
        Object singleResult = q.getSingleResult();
        if (singleResult instanceof List) {
            Object first = ((List) singleResult).get(0);
            if (first instanceof Double) {
                return (Double) first;
            } else {
                return ((Number) first).doubleValue();
            }
        }
        return null;
    }

    public void calculateValueProposition(ValuePropositionPerformance vpf) {
        for (ValuePropositionComponentPerformance o : vpf.getComponents()) {
            Map<String, Measurement> measurements = new HashMap<String, Measurement>();
            Set<Measurement> nonAggregatingMeasures = new HashSet<Measurement>();
            for (Measurement measurement : o.getMeasurements()) {
                measurements.put(measurement.getMeasure().getUri(), measurement);
                String additionalCriteria = "and m.deliverableFlow.fromActivity.performer= :provider and m.deliverableFlow.toActivity.performer= :receiver";
                if (measurement.getMeasure() instanceof CollectiveMeasure) {
                    CollectiveMeasure cm = (CollectiveMeasure) measurement.getMeasure();
                    Accumulator accumulator = cm.getAccumulator();
                    Query q = entityManager.createQuery("select " + accumulator.name() + "(m.value) from ValueAddMeasurement m where m.measure.uri in :measureUri " + additionalCriteria);
                    Set<String> uris = new HashSet<String>();
                    for (EmfReference emfReference : cm.getAggregatedMeasures()) {
                        uris.add(emfReference.getUri());
                    }
                    q.setParameter("measureUri", uris);
                    q.setParameter("provider", vpf.getProvider());
                    q.setParameter("receiver", vpf.getReceiver());
                    measurement.setActualValue(extractDouble(q));
                } else if (measurement.getMeasure() instanceof CountingMeasure) {
                    CountingMeasure cm = (CountingMeasure) measurement.getMeasure();
                    Query q = entityManager.createQuery("select count(m) from ValueAddMeasurement m where m.measure.uri =:measureUri and m.value is not null and " + additionalCriteria + " and " + cm.getValuesToCount());
                    q.setParameter("measureUri", cm.getMeasureToCount().getUri());
                    q.setParameter("provider", vpf.getProvider());
                    q.setParameter("receiver", vpf.getReceiver());
                    measurement.setActualValue(extractDouble(q));
                } else {
                    nonAggregatingMeasures.add(measurement);
                }
            }
            resolveMeasurements(vpf.getValueProposition().getCollaboration().getDeploymentId(), measurements, nonAggregatingMeasures);
        }
    }

    private void resolveMeasurement(String deploymentId, Map<String, Measurement> context, Measurement measurement) {
        if (measurement.getMeasure() instanceof BinaryMeasure) {
            BinaryMeasure bm = (BinaryMeasure) measurement.getMeasure();
            Measurement measurementA = resolveMeasurement(deploymentId, context, bm.getMeasureA());
            Measurement measurementB = resolveMeasurement(deploymentId, context, bm.getMeasureB());
            if (measurementA != null && measurementA.getActualValue() != null && measurementB != null && measurementB.getActualValue() != null) {
                measurement.setActualValue(bm.getFunctor().apply(measurementA.getActualValue(), measurementB.getActualValue()));
            }
        } else if (measurement.getMeasure() instanceof RescaledMeasure) {
            RescaledMeasure bm = (RescaledMeasure) measurement.getMeasure();
            Measurement measurementA = resolveMeasurement(deploymentId, context, bm.getRescaledMeasure());
            if (measurementA != null && measurementA.getActualValue() != null) {
                measurement.setActualValue(bm.getMultiplier() * measurementA.getActualValue() + bm.getOffset());
            }
        } else if (measurement.getMeasure() instanceof NamedMeasure) {
            NamedMeasure bm = (NamedMeasure) measurement.getMeasure();
            NamedMeasureStrategy strategy = NamedMeasureRegistry.get(deploymentId, bm.getName());
            if (strategy != null) {
                Object o = strategy.applyMeasurement(measurement.getMeasurand());
                if (o instanceof Double) {
                    measurement.setActualValue((Double) o);
                } else if (o instanceof Enum) {
                    measurement.setActualRating((Enum) o);
                }
            }
        }
    }

    private Measurement resolveMeasurement(String deploymentId, Map<String, Measurement> context, EmfReference measureA) {
        Measurement measurement = context.get(measureA.getUri());
        if (measurement != null && measurement.getActualValue() == null) {
            resolveMeasurement(deploymentId, context, measurement);
        }
        return measurement;
    }

}
