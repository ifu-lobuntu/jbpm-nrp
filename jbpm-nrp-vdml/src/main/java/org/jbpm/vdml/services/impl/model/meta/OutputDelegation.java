package org.jbpm.vdml.services.impl.model.meta;


import javax.persistence.*;

@Entity
@DiscriminatorValue("OutputDelegation")
public class OutputDelegation extends DirectedFlow {

    public OutputDelegation() {
    }

    @Override
    public OutputPort getTarget() {
        return (OutputPort) super.getTarget();
    }

    @Override
    public OutputPort getSource() {
        return (OutputPort) super.getSource();
    }

    public OutputDelegation(String uri, Collaboration owningCollaboration) {
        super(uri, owningCollaboration);
    }


}
