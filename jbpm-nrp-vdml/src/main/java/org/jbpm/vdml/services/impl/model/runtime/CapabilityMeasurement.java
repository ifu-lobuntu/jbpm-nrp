package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class CapabilityMeasurement extends Measurement{
    @ManyToOne
    private CapabilityOffer capabilityOffer;

    public CapabilityMeasurement() {
    }

    public CapabilityMeasurement(Measure measure, CapabilityOffer capabilityOffer) {
        super(measure);
        this.capabilityOffer = capabilityOffer;
        this.capabilityOffer.getMeasurements().add(this);
    }

    public CapabilityOffer getCapabilityOffer() {
        return capabilityOffer;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getCapabilityOffer();
    }
}
