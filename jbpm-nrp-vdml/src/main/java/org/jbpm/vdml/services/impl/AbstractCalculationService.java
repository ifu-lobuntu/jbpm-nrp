package org.jbpm.vdml.services.impl;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.Measurement;
import org.jbpm.vdml.services.impl.model.runtime.StoreMeasurement;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AbstractCalculationService {
    protected EntityManager entityManager;

    public AbstractCalculationService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AbstractCalculationService() {
    }

    protected void resolveObservedMeasures(String deploymentId, ObservationContext context, Collection<? extends Measurement> measurements) {
        for (Measurement measurement : measurements) {
            if(!measurement.isResolved()) {
                resolveMeasurement(deploymentId, context, measurement);
            }
        }
    }
    protected void resolveAllMeasurements(String deploymentId, ObservationContext c) {
        float oldResolutionRatio = 0;
        float newResolutionRatio = 0;
        do {
            oldResolutionRatio = newResolutionRatio;
            resolveObservedMeasures(deploymentId, c, c.getAllMeasurements());
            newResolutionRatio = c.calculateResolutionRate();
        } while (oldResolutionRatio < newResolutionRatio && newResolutionRatio!=1f);
    }

    private void resolveMeasurement(String deploymentId, ObservationContext context, Measurement measurement) {
        if (measurement.getMeasure() instanceof BinaryMeasure) {
            BinaryMeasure bm = (BinaryMeasure) measurement.getMeasure();
            Double measurementA = resolveMeasurement(deploymentId, context, bm.getMeasureA());
            Double measurementB = resolveMeasurement(deploymentId, context, bm.getMeasureB());
            if (measurementA != null && measurementB != null) {
                measurement.setActualValue(bm.getFunctor().apply(measurementA, measurementB));
            }
        } else if (measurement.getMeasure() instanceof RescaledMeasure) {
            RescaledMeasure bm = (RescaledMeasure) measurement.getMeasure();
            Double measurementA = resolveMeasurement(deploymentId, context, bm.getRescaledMeasure());
            if (measurementA != null) {
                measurement.setActualValue(bm.getMultiplier() * measurementA + bm.getOffset());
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
        } else if (measurement.getMeasure() instanceof CollectiveMeasure) {
            CollectiveMeasure collectiveMeasure = (CollectiveMeasure) measurement.getMeasure();
            Object values = resolveMeasurements(deploymentId, context, collectiveMeasure.getAggregatedMeasures(), false,true);
            if (values != null) {
                double value = calculateCollectiveMeasure(collectiveMeasure, (double[]) values);
                measurement.setActualValue(value);
            }
        } else if (measurement.getMeasure() instanceof CountingMeasure) {
            CountingMeasure collectiveMeasure = (CountingMeasure) measurement.getMeasure();
            String exp = collectiveMeasure.getValuesToCount();
            Object values = resolveMeasurements(deploymentId, context, Collections.singleton(collectiveMeasure.getMeasureToCount()), exp!=null && exp.contains("actualRating"),true);
            if (values != null) {
                Counter c = new Counter(collectiveMeasure.getValuesToCount());
                double value = values instanceof double[] ? c.getValue((double[]) values) : c.getValue((Enum<?>[]) values);
                measurement.setActualValue(value);
            }
        }
    }


    private double calculateCollectiveMeasure(CollectiveMeasure collectiveMeasure, double[] values) {
        DescriptiveStatistics ds = new DescriptiveStatistics(values);
        switch (collectiveMeasure.getAccumulator()) {
            case AVERAGE:
                return ds.getSum() / values.length;
            case MAX:
                return ds.getMax();
            case MIN:
                return ds.getMin();
            case PRODUCT:
                double value = 1;
                for (double v : values) {
                    value *= v;
                }
                return value;
            case STANDARD_DEVIATION:
                return ds.getStandardDeviation();
            case SUM:
                return ds.getSum();
            default:
                throw new IllegalStateException("Accumulator " + collectiveMeasure.getAccumulator().name() + "not supported yet.");
        }
    }

    private Double resolveMeasurement(String deploymentId, ObservationContext context, EmfReference measureA) {
        Collection<Measurement> measurements = resolveObservedMeasures(deploymentId, context, measureA);
        if (!measurements.isEmpty()) {
            return measurements.iterator().next().getActualValue();
        }
        return null;
    }

    private Object resolveMeasurements(String deploymentId, ObservationContext context, Set<EmfReference> measureA, boolean returnRatings, boolean interruptOnNullMeasurement) {
        Set<Measurement> all = new HashSet<Measurement>();
        int unresolvedMeasurementCount=0;
        int totalMeasurementCount=0;
        for (EmfReference emfReference : measureA) {
            Collection<Measurement> measurements = resolveObservedMeasures(deploymentId, context, emfReference);
            if (measurements.isEmpty()) {
                if(!interruptOnNullMeasurement) {
//                    System.out.println("Measurement not in context");
                }
                return null;// context does not include all the measures yet
            } else {
                for (Measurement measurement : measurements) {
                    totalMeasurementCount++;
                    if (measurement.getActualRating() == null) {
                        if (returnRatings) {
                            return null;
                        } else if (measurement.getActualValue() == null) {
                            if(interruptOnNullMeasurement) {
                                return null;
                            }else{
                                unresolvedMeasurementCount++;
                            }

                        }
                    }
                    all.add(measurement);
                }
            }
        }
        if(!interruptOnNullMeasurement){
//            System.out.println(unresolvedMeasurementCount + " measurements from "+totalMeasurementCount+" not calculated yet");
            return null;
        }
        if (returnRatings) {
            return populateEnums(all);
        } else {
            return populateNumbers(all);
        }
    }

    private double[] populateNumbers(Set<Measurement> all) {
        double[] result = new double[all.size()];
        int i = 0;
        for (Measurement measurement : all) {
            result[i] = measurement.getActualValue();
            i++;
        }
        return result;
    }

    private Enum<?>[] populateEnums(Set<Measurement> all) {
        Enum<?>[] result = new Enum<?>[all.size()];
        int i = 0;
        for (Measurement measurement : all) {
            result[i] = measurement.getActualRating();
            i++;
        }
        return result;
    }

    private Collection<Measurement> resolveObservedMeasures(String deploymentId, ObservationContext context, EmfReference measureA) {
        ObservedMeasure observedMeasure = context.getObservedMeasure(measureA.getUri());
        Collection<Measurement> measurements = java.util.Collections.emptySet();
        if (observedMeasure != null) {
            measurements = observedMeasure.getMeasurements();
            for (Measurement measurement : measurements) {
                if (!measurement.isResolved()) {
                    resolveMeasurement(deploymentId, context, measurement);
                }
            }
        }
        return measurements;
    }
}
