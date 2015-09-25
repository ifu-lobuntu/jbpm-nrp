package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.model.meta.Accumulator;
import org.jbpm.vdml.services.impl.model.meta.CollectiveMeasure;
import org.jbpm.vdml.services.impl.model.meta.CountingMeasure;
import org.jbpm.vdml.services.impl.model.meta.EmfReference;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

public class PerformanceCalculationService extends AbstractCalculationService {

    public PerformanceCalculationService(EntityManager entityManager) {
        super(entityManager);
    }

    public PerformanceCalculationService() {
    }

    public void calculateProvidedValueProposition(ProvidedValuePropositionPerformance vpf) {
        //similar to calculateValueProposition without the receiver as criterion
    }


    public CapabilityPerformance findCapabilityPerformance(Long cpId) {
        return entityManager.find(CapabilityPerformance.class,cpId);
    }
    public void calculateCapabilityPerformance(Long cpId) {
        CapabilityPerformance measurand=entityManager.find(CapabilityPerformance.class,cpId);
        Map<String, Measurement> context = new HashMap<String, Measurement>();
        addToContext(context,measurand.getMeasurements());
        Set<Measurement> otherMeasurements = resolveAggregatedMEasurements(measurand, "ActivityMeasurement m where m.activity.capabilityOffer");
        resolveMeasurements(measurand.getCapability().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }

    private Set<Measurement> resolveAggregatedMEasurements(Measurand measurand, String measurandMatcher) {
        Set<Measurement> otherMeasurements = new HashSet<Measurement>();
        for (Measurement measurement : measurand.getMeasurements()) {
            if (measurement.getMeasure() instanceof CollectiveMeasure) {
                CollectiveMeasure cm = (CollectiveMeasure) measurement.getMeasure();
                Accumulator accumulator = cm.getAccumulator();
                Query q = entityManager.createQuery("select "+ constructFormula(accumulator) +" from "+ measurandMatcher +" =:measurand and m.measure.uri in :measureUris");
                Set<String> uris = new HashSet<String>();
                for (EmfReference emfReference : cm.getAggregatedMeasures()) {
                    uris.add(emfReference.getUri());
                }
                q.setParameter("measureUris", uris);
                q.setParameter("measurand", measurand);
                measurement.setActualValue(extractDouble(q));
            } else if (measurement.getMeasure() instanceof CountingMeasure) {
                CountingMeasure cm = (CountingMeasure) measurement.getMeasure();
                Query q = entityManager.createQuery("select count(m)  from "+ measurandMatcher +" =:measurand and m.measure.uri =:measureUri and " + cm.getValuesToCount());
                q.setParameter("measureUri", cm.getMeasureToCount().getUri());
                q.setParameter("measurand", measurand);
                measurement.setActualValue(extractDouble(q));
            } else {
                otherMeasurements.add(measurement);
            }
        }
        return otherMeasurements;
    }

    private String constructFormula(Accumulator accumulator) {
        switch (accumulator){
            case AVERAGE:
                return "AVG(m.actualValue)";
            case SUM:
                return "SUM(m.actualValue)";
            case MAX:
                return "MAX(m.actualValue)";
            case MIN:
                return "MIN(m.actualValue)";
            case STANDARD_DEVIATION:
                return "sqrt((sum(m.actualValue*m.actualValue)/count(m.actualValue)) - (avg(m.actualValue) * avg(m.actualValue)))";

            //TODO product
        }
        return "AVG(m.actualValue)";
    }

    public void calculateStorePerformance(Long spId) {
        StorePerformance measurand=entityManager.find(StorePerformance.class,spId);
        Map<String, Measurement> context = new HashMap<String, Measurement>();
        addToContext(context,measurand.getMeasurements());
        Set<Measurement> otherMeasurements = resolveAggregatedMEasurements(measurand, "SupplyingStoreMeasurement m where m.store.store");
        resolveMeasurements( measurand.getStoreDefinition().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }
    public StorePerformance findStorePerformance(Long spId) {
        return entityManager.find(StorePerformance.class,spId);
    }

    public void calculateReusableResourcePerformance(Long bipId) {
        ReusableBusinessItemPerformance measurand=entityManager.find(ReusableBusinessItemPerformance.class,bipId);
        Map<String, Measurement> context = new HashMap<String, Measurement>();
        addToContext(context,measurand.getMeasurements());
        Set<Measurement> otherMeasurements = resolveAggregatedMEasurements(measurand, "BusinessItemMeasurement m where m.businessItem.instanceReference");
        resolveMeasurements( measurand.getDefinition().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }

    private Double extractDouble(Query q) {
        Object singleResult = q.getSingleResult();
        if (singleResult instanceof Number) {
            return ((Number) singleResult).doubleValue();
        }else
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
                    Query q = entityManager.createQuery("select " + constructFormula(accumulator) + "(m.value) from ValueAddMeasurement m where m.measure.uri in :measureUri " + additionalCriteria);
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
                    Query q = entityManager.createQuery("select count(m) from ValueAddMeasurement m where m.measure.uri =:measureUri and m.actualValue is not null and " + additionalCriteria + " and " + cm.getValuesToCount());
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

    public ReusableBusinessItemPerformance findReusableBusinessItemPerformance(Long bipId) {
        return entityManager.find(ReusableBusinessItemPerformance.class,bipId);
    }
}
