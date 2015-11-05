package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ValuePropositionComponentInstanceMeasurement extends Measurement{
    @ManyToOne
    private ValuePropositionComponentInstance valuePropositionComponent;

    public ValuePropositionComponentInstanceMeasurement(Measure measure, ValuePropositionComponentInstance valuePropositionComponent) {
        super(measure);
        this.valuePropositionComponent = valuePropositionComponent;
        this.valuePropositionComponent.getMeasurements().add(this);
    }

    public ValuePropositionComponentInstanceMeasurement() {
    }

    public ValuePropositionComponentInstance getValueElement() {
        return valuePropositionComponent;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getValueElement();
    }
}
