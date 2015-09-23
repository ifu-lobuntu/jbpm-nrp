package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class CapabilityMeasurement extends Measurement{
    @ManyToOne
    private CapabilityPerformance capabilityPerformance;

    public CapabilityMeasurement() {
    }

    public CapabilityMeasurement(Measure measure, CapabilityPerformance capabilityPerformance) {
        super(measure);
        this.capabilityPerformance = capabilityPerformance;
        this.capabilityPerformance.getMeasurements().add(this);
    }

    public CapabilityPerformance getCapabilityPerformance() {
        return capabilityPerformance;
    }
}
