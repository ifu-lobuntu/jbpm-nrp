package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.ValueAdd;
import org.jbpm.vdml.services.impl.model.meta.ValueElement;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity

@DiscriminatorValue("ValueAddInstance")
public class ValueAddInstance extends ValueElementInstance{
    @ManyToOne
    private OutputPortInstance outputPort;
    @OneToMany(mappedBy = "valueAdd",cascade = CascadeType.ALL)
    private Set<ValueAddInstanceMeasurement> measurements=new HashSet<ValueAddInstanceMeasurement>();

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

    public ValueAddInstanceMeasurement getValueMeasurement(){
        return findMatchingRuntimeEntity(getMeasurements(),getValueElement().getValueMeasure());
    }

    public Set<ValueAddInstanceMeasurement> getMeasurements() {
        return measurements;
    }
}
