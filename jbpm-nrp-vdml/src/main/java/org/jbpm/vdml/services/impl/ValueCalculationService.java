package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

public class ValueCalculationService {
    private EntityManager entityManager;

    public void calculateProvidedValueProposition(ProvidedValuePropositionPerformance vpf) {
        //similar to calculateValueProposition without the receiver as criterion
    }
    public void doCollaborationObservations(CollaborationObservation collaboration) {
        //First business items
        for (BusinessItemObservation businessItem: collaboration.getBusinessItems()) {
            Set<? extends Measurement> measurements = businessItem.getMeasurements();
            Map<String,Measurement> context=new HashMap<String, Measurement>();
            addToContext(context, measurements);
            resolveMeasurements(context, measurements);

        }
        //Then the flows
        for (DirectedFlowObservation flow : collaboration.getOwnedDirectedFlows()) {
            Map<String,Measurement> context=new HashMap<String, Measurement>();
            addFlowToContext(flow, context);
            resolveMeasurements(context, flow.getValueAddMeasurements());
        }
        //Then the activities?
        for (ActivityObservation a : collaboration.getActivities()) {
            Map<String,Measurement> context=new HashMap<String, Measurement>();
            addToContext(context, a.getMeasurements());
            for (DirectedFlowObservation flow : a.getConcludedFlow()) {
                addFlowToContext(flow, context);
            }
            for (DirectedFlowObservation flow : a.getCommencedFlow()) {
                addFlowToContext(flow, context);
            }
            resolveMeasurements(context, a.getMeasurements());
        }
    }

    private void addFlowToContext(DirectedFlowObservation flow, Map<String, Measurement> context) {
        addToContext(context, flow.getDeliverable().getMeasurements());
        addToContext(context, flow.getMeasurements());
        addToContext(context, flow.getValueAddMeasurements());
    }

    private void resolveMeasurements(Map<String, Measurement> context, Set<? extends Measurement> measurements) {
        for (Measurement measurement : measurements) {
            resolveMeasurement(context, measurement);
        }
    }

    private void addToContext(Map<String, Measurement> context, Set<? extends Measurement> measurements) {
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
                measurement.setValue(extractDouble(q));
            } else if (measurement.getMeasure() instanceof CountingMeasure) {
                CountingMeasure cm = (CountingMeasure) measurement.getMeasure();
                Query q = entityManager.createQuery("select count(m) from ActivityMeasurement m where m.measure.uri =:measureUri and m.value is not null " + additionalCriteria + " and " + cm.getValuesToCount());
                q.setParameter("measureUri", cm.getMeasureToCount().getUri());
                q.setParameter("performer", cp.getParticipant());
                measurement.setValue(extractDouble(q));
            } else {
                otherMeasurements.add(measurement);
            }
        }
        resolveDerivedMeasures(otherMeasurements, measurements);
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
                measurement.setValue(extractDouble(q));
            } else if (measurement.getMeasure() instanceof CountingMeasure) {
                CountingMeasure cm = (CountingMeasure) measurement.getMeasure();
                Query q = entityManager.createQuery("select count(m) from DirectedFlowObservation d inner join d.deliverable.businessItem where d.sourcePortContainer = :store and m.measure.uri in :measureUri and m.value is not null " + additionalCriteria + " and " + cm.getValuesToCount());
                q.setParameter("measureUri", cm.getMeasureToCount().getUri());
                q.setParameter("store", rsp);
                measurement.setValue(extractDouble(q));
            } else {
                otherMeasurements.add(measurement);
            }
        }
        resolveDerivedMeasures(otherMeasurements, measurements);
    }

    private Double extractDouble(Query q) {
        Object singleResult=q.getSingleResult();
        if(singleResult instanceof List){
            Object first=((List) singleResult).get(0);
            if(first instanceof Double){
                return (Double) first;
            }else{
                return ((Number)first).doubleValue();
            }
        }
        return null;
    }

    public void calculateValueProposition(ValuePropositionPerformance vpf) {
        for (ValuePropositionComponentPerformance o : vpf.getComponents()) {
            Map<String, Measurement> measurements = new HashMap<String, Measurement>();
            Set<Measurement> otherMeasurements = new HashSet<Measurement>();
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
                    measurement.setValue(extractDouble(q));
                } else if (measurement.getMeasure() instanceof CountingMeasure) {
                    CountingMeasure cm = (CountingMeasure) measurement.getMeasure();
                    Query q = entityManager.createQuery("select count(m) from ValueAddMeasurement m where m.measure.uri =:measureUri and m.value is not null and " + additionalCriteria + " and " + cm.getValuesToCount());
                    q.setParameter("measureUri", cm.getMeasureToCount().getUri());
                    q.setParameter("provider", vpf.getProvider());
                    q.setParameter("receiver", vpf.getReceiver());
                    measurement.setValue(extractDouble(q));
                } else {
                    otherMeasurements.add(measurement);
                }
            }
            resolveDerivedMeasures(otherMeasurements, measurements);
        }
    }

    private void resolveDerivedMeasures(Set<Measurement> otherMeasurements, Map<String, Measurement> context) {
        resolveMeasurements(context, otherMeasurements);
    }

    private void resolveMeasurement(Map<String, Measurement> context, Measurement measurement) {
        if (measurement.getMeasure() instanceof BinaryMeasure) {
            BinaryMeasure bm = (BinaryMeasure) measurement.getMeasure();
            Measurement measurementA = resolveMeasurement(context, bm.getMeasureA());
            Measurement measurementB = resolveMeasurement(context, bm.getMeasureB());
            if (measurementA.getValue() != null && measurementB.getValue() != null) {
                measurement.setValue(bm.getFunctor().apply(measurementA.getValue(), measurementB.getValue()));
            }
        } else if (measurement.getMeasure() instanceof RescaledMeasure) {
            RescaledMeasure bm = (RescaledMeasure) measurement.getMeasure();
            Measurement measurementA = resolveMeasurement(context, bm.getRescaledMeasure());
            if (measurementA.getValue() != null) {
                measurement.setValue(bm.getMultiplier() * measurementA.getValue() + bm.getOffset());
            }
        }
    }

    private Measurement resolveMeasurement(Map<String, Measurement> context, EmfReference measureA) {
        Measurement measurement = context.get(measureA.getUri());
        if (measurement.getValue() == null) {
            resolveMeasurement(context, measurement);
        }
        return measurement;
    }

}
