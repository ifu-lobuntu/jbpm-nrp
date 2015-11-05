package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ValuePropositionComponentPerformanceMeasurement extends Measurement{
    @ManyToOne
    private ValuePropositionComponentPerformance component;

    public ValuePropositionComponentPerformanceMeasurement(Measure measure, ValuePropositionComponentPerformance component) {
        super(measure);
        this.component = component;
        this.component.getMeasurements().add(this);
    }

    public ValuePropositionComponentPerformanceMeasurement() {
    }

    public ValuePropositionComponentPerformance getComponent() {
        return component;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getComponent();
    }
}
