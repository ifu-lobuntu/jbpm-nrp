package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.meta.Accumulator;
import org.jbpm.vdml.services.impl.model.meta.CollectiveMeasure;
import org.jbpm.vdml.services.impl.model.meta.CountingMeasure;
import org.jbpm.vdml.services.impl.model.meta.EmfReference;
import org.jbpm.vdml.services.impl.model.runtime.Measurand;
import org.jbpm.vdml.services.impl.model.runtime.Measurement;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashSet;
import java.util.Set;


public class MeasurementAggregator <T extends Measurand> {
    private EntityManager entityManager;

    public MeasurementAggregator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Set<Measurement> resolveAggregatedMEasurements(T measurand, String measurandMatcher) {
        Set<Measurement> otherMeasurements = new HashSet<Measurement>();
        for (Measurement measurement : measurand.getMeasurements()) {
            if (measurement.getMeasure() instanceof CollectiveMeasure) {
                CollectiveMeasure cm = (CollectiveMeasure) measurement.getMeasure();
                Accumulator accumulator = cm.getAccumulator();
                Query q = entityManager.createQuery("select " + constructFormula(accumulator) + " from " + measurandMatcher + " =:measurand and m.measure.uri in :measureUris");
                Set<String> uris = new HashSet<String>();
                for (EmfReference emfReference : cm.getAggregatedMeasures()) {
                    uris.add(emfReference.getUri());
                }
                q.setParameter("measureUris", uris);
                addAdditionalParameters(q,measurand);
                measurement.setActualValue(extractDouble(q));
            } else if (measurement.getMeasure() instanceof CountingMeasure) {
                CountingMeasure cm = (CountingMeasure) measurement.getMeasure();
                Query q = entityManager.createQuery("select count(m)  from " + measurandMatcher + " =:measurand and m.measure.uri =:measureUri and " + cm.getValuesToCount());
                q.setParameter("measureUri", cm.getMeasureToCount().getUri());
                addAdditionalParameters(q,measurand);
                measurement.setActualValue(extractDouble(q));
            } else {
                otherMeasurements.add(measurement);
            }
        }
        return otherMeasurements;
    }
    protected void addAdditionalParameters(Query q,T measurand){
        q.setParameter("measurand", measurand);
    }

    private String constructFormula(Accumulator accumulator) {
        switch (accumulator) {
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

    private Double extractDouble(Query q) {
        Object singleResult = q.getSingleResult();
        if (singleResult instanceof Number) {
            return ((Number) singleResult).doubleValue();
        } else
            return null;
    }
}
