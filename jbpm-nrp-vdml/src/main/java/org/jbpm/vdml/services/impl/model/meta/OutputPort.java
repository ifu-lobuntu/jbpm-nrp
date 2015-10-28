package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("OutputPort")

public class OutputPort extends Port{
    @ManyToOne
    private PortContainer portContainer;
    @ManyToMany

    @JoinTable(name="output_port_value_add")
    private Set<Measure> valueAdds=new HashSet<Measure>();
    public OutputPort(String uri,PortContainer portContainer) {
        super(uri);
        this.portContainer=portContainer;
        this.portContainer.getOutput().add(this);
    }

    public OutputPort() {
        super();
    }

    @Override
    public PortContainer getPortContainer() {
        return portContainer;
    }

    public Set<OutputDelegation> getDelegatedOutputs() {
        return getOutputDelegations(super.getInflows());
    }
    public DeliverableFlow getOutput(){
        for (DirectedFlow directedFlow : getOutflows()) {
            if(directedFlow instanceof DeliverableFlow){
                return (DeliverableFlow) directedFlow;
            }
        }
        return null;
    }
    protected Set<OutputDelegation> getOutputDelegations(Set<DirectedFlow> inflows) {
        Set<OutputDelegation> delegatedOutputs=new HashSet<OutputDelegation>();
        for (DirectedFlow inflow : inflows) {
            if(inflow instanceof OutputDelegation){
                delegatedOutputs.add((OutputDelegation) inflow);
            }
        }
        return delegatedOutputs;
    }

    public Set<Measure> getValueAdds() {
        return valueAdds;
    }

    public Set<OutputDelegation> getOutputDelegations() {
        return getOutputDelegations(super.getOutflows());
    }
}
