package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("InputPort")

public class InputPort extends Port{

    @ManyToOne
    private PortContainer portContainer;

    public InputPort(String uri,PortContainer portContainer) {
        super(uri);
        this.portContainer=portContainer;
        this.portContainer.getInput().add(this);
    }

    public InputPort() {
        super();
    }

    @Override
    public PortContainer getPortContainer() {
        return portContainer;
    }

    public Set<InputDelegation> getDelegatedInputs() {
        return getOutputDelegations(super.getInflows());
    }
    public DeliverableFlow getInput(){
        for (DirectedFlow directedFlow : getInflows()) {
            if(directedFlow instanceof DeliverableFlow){
                return (DeliverableFlow) directedFlow;
            }
        }
        return null;
    }
    protected Set<InputDelegation> getOutputDelegations(Set<DirectedFlow> inflows) {
        Set<InputDelegation> delegatedOutputs=new HashSet<InputDelegation>();
        for (DirectedFlow inflow : inflows) {
            if(inflow instanceof InputDelegation){
                delegatedOutputs.add((InputDelegation) inflow);
            }
        }
        return delegatedOutputs;
    }

    public Set<InputDelegation> getInputDelegations() {
        return getOutputDelegations(super.getOutflows());
    }


}
