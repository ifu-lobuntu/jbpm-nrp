package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ValueElementInstanceMeasurement extends Measurement{
    @ManyToOne
    private ValueElementInstance valueElement;

    public ValueElementInstanceMeasurement(Measure measure, ValueElementInstance component) {
        super(measure);
        this.valueElement = component;
        this.valueElement.getMeasurements().add(this);
    }

    public ValueElementInstanceMeasurement() {
    }

    public ValueElementInstance getValueElement() {
        return valueElement;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getValueElement();
    }
}
