package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ValuePropositionComponentMeasurement extends Measurement{
    @ManyToOne
    private ValuePropositionComponentPerformance component;

    public ValuePropositionComponentMeasurement(Measure measure, ValuePropositionComponentPerformance component) {
        super(measure);
        this.component = component;
        this.component.getMeasurements().add(this);
    }

    public ValuePropositionComponentMeasurement() {
    }

    public ValuePropositionComponentPerformance getComponent() {
        return component;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getComponent();
    }
}
