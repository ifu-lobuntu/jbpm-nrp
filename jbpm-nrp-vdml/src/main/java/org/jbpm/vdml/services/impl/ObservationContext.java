package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.runtime.Measurement;

import java.util.*;

public class ObservationContext {
    private Map<String,ObservedMeasure> observedMeasures=new HashMap<String, ObservedMeasure>();
    private ObservationPhase phase;

    public ObservationContext(ObservationPhase phase) {
        this.phase = phase;
    }

    public ObservationPhase getPhase() {
        return phase;
    }

    public ObservedMeasure getObservedMeasure(String key) {
        return observedMeasures.get(key);
    }

    public void put(Measurement measurement) {
        ObservedMeasure om = getObservedMeasure(measurement.getMeasure().getUri());
        if(om==null){
            observedMeasures.put(measurement.getMeasure().getUri(),om=new ObservedMeasure(measurement.getMeasure()));
        }
        om.getMeasurements().add(measurement);
    }

    public void putAll(Collection<? extends Measurement> measurements) {
        for (Measurement measurement : measurements) {
            put(measurement);
        }
    }
    public ObservationContext subContext(){
        ObservationContext result = new ObservationContext(phase);
        result.observedMeasures.putAll(observedMeasures);
        return result;
    }
    public float calculateResolutionRate(){
        float resolvedCount=0;
        float totalCount=0;
        for (ObservedMeasure om : observedMeasures.values()) {
            for (Measurement measurement : om.getMeasurements()) {
                totalCount++;
                if(phase.isResolved(measurement)){
                    resolvedCount++;
                }
            }
        }
        return resolvedCount/totalCount;
    }
    public Collection<Measurement> getAllMeasurements(){
        Collection<Measurement> result = new HashSet<Measurement>();
        for (ObservedMeasure om : observedMeasures.values()) {
            result.addAll(om.getMeasurements());
        }
        return result;
    }


}
