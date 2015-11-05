package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class PortMeasurement extends Measurement{
    @ManyToOne
    private PortInstance port;
    @Override
    public RuntimeEntity getMeasurand() {
        return port;
    }

    public PortMeasurement() {
    }

    public PortMeasurement(Measure measure, PortInstance port) {
        super(measure);
        this.port = port;
        this.port.getMeasurements().add(this);
    }

    public PortInstance getPort() {
        return port;
    }
}
