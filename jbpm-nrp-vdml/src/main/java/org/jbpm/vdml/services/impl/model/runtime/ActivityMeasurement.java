package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ActivityMeasurement extends Measurement{
    @ManyToOne
    private ActivityInstance activity;

    public ActivityMeasurement() {
    }

    public ActivityMeasurement(Measure measure, ActivityInstance activity){
        super(measure);
        this.activity=activity;
        activity.getMeasurements().add(this);
    }

    public ActivityInstance getActivity() {
        return activity;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getActivity();
    }
}
