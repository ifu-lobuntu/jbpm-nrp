package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ValueAddInstanceMeasurement extends Measurement{
    @ManyToOne
    private ValueAddInstance valueAdd;

    public ValueAddInstanceMeasurement(Measure measure, ValueAddInstance valueAdd) {
        super(measure);
        this.valueAdd = valueAdd;
        this.valueAdd.getMeasurements().add(this);
    }

    public ValueAddInstanceMeasurement() {
    }

    public ValueAddInstance getValueElement() {
        return valueAdd;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getValueElement();
    }
}
