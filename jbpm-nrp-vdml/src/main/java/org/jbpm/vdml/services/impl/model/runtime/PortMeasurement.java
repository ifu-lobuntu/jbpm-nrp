package org.jbpm.vdml.services.impl.model.runtime;

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
}
