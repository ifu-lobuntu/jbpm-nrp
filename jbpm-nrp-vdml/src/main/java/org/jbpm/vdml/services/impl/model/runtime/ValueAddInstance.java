package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.ValueAdd;
import org.jbpm.vdml.services.impl.model.meta.ValueElement;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity

@DiscriminatorValue("ValueAddInstance")
public class ValueAddInstance extends ValueElementInstance{
    @ManyToOne
    private OutputPortInstance outputPort;

    public ValueAddInstance(ValueElement valueElement, OutputPortInstance outputPort) {
        super(valueElement);
        this.outputPort=outputPort;
        this.outputPort.getValueAdds().add(this);
    }

    public ValueAddInstance() {
    }

    public OutputPortInstance getOutputPort() {
        return outputPort;
    }

    public ValueAdd getValueAdd() {
        return (ValueAdd) super.getValueElement();
    }

    public Measurement getValueMeasurement() {
        return findMatchingRuntimeEntity(getMeasurements(), getValueAdd().getValueMeasure());
    }
}
