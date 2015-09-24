package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.meta.BinaryMeasure;
import org.jbpm.vdml.services.impl.model.meta.EmfReference;
import org.jbpm.vdml.services.impl.model.meta.NamedMeasure;
import org.jbpm.vdml.services.impl.model.meta.RescaledMeasure;
import org.jbpm.vdml.services.impl.model.runtime.Measurement;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Map;

public class AbstractCalculationService {
    protected EntityManager entityManager;

    public AbstractCalculationService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AbstractCalculationService() {
    }

    protected void resolveMeasurements(String deploymentId, Map<String, Measurement> context, Collection<? extends Measurement> measurements) {
        for (Measurement measurement : measurements) {
            resolveMeasurement(deploymentId, context, measurement);
        }
    }

    protected void addToContext(Map<String, Measurement> context, Collection<? extends Measurement> measurements) {
        for (Measurement measurement : measurements) {
            context.put(measurement.getMeasure().getUri(), measurement);
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
