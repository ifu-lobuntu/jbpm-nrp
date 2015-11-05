package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.OutputPort;
import org.jbpm.vdml.services.impl.model.meta.ValueAdd;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
@DiscriminatorValue("OutputPortInstance")
public class OutputPortInstance extends PortInstance {
    @OneToMany(mappedBy = "outputPort", cascade = CascadeType.ALL)
    private Set<ValueAddInstance> valueAdds=new HashSet<ValueAddInstance>();
    @ManyToOne
    private ValuePropositionInstance valueProposition;
    public OutputPortInstance() {
    }
    public DeliverableFlowInstance getOutput(){
        if(getOutflow().size()>1){
            throw new IllegalStateException("Port '" + getPort().getName() +"' has more that one output DeliverableFlow");
        }
        return getOutflow().iterator().next();
    }

    public ValuePropositionInstance getValueProposition() {
        return valueProposition;
    }

    public void setValueProposition(ValuePropositionInstance valueProposition) {
        this.valueProposition = valueProposition;
    }

    public OutputPort getPort(){
        return (OutputPort) super.getPort();
    }
    public OutputPortInstance(OutputPort port, PortContainerInstance portContainer) {
        super(port, portContainer);
    }

    public Set<ValueAddInstance> getValueAdds() {
        return valueAdds;
    }

    public ValueAddInstance findValueAdd(ValueAdd valueAdd) {
        return findMatchingRuntimeEntity(getValueAdds(), valueAdd);
    }
}
