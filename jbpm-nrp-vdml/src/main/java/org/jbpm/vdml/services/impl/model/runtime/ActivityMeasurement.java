package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ActivityMeasurement extends Measurement{
    @ManyToOne
    private ActivityObservation activity;

    public ActivityMeasurement() {
    }

    public ActivityMeasurement(Measure measure, ActivityObservation activity){
        super(measure);
        this.activity=activity;
        activity.getMeasurements().add(this);
    }

    public ActivityObservation getActivity() {
        return activity;
    }
}
