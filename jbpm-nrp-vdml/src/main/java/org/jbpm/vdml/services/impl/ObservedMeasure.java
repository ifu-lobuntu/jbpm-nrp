package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.runtime.Measurement;

import java.util.Collection;
import java.util.HashSet;

public class ObservedMeasure {
    private Measure measure;
    private Collection<Measurement> measurements=new HashSet<Measurement>();

    public ObservedMeasure(Measure measure) {
        this.measure = measure;
    }
    public String getKey(){
        return measure.getUri();
    }

    public Collection<Measurement> getMeasurements() {
        return measurements;
    }
}
