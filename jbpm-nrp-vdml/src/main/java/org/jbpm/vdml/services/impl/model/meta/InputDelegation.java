package org.jbpm.vdml.services.impl.model.meta;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("InputDelegation")
public class InputDelegation extends DirectedFlow {

    public InputDelegation() {
    }

    @Override
    public InputPort getSource() {
        return (InputPort) super.getSource();
    }

    @Override
    public InputPort getTarget() {
        return (InputPort) super.getTarget();
    }

    public InputDelegation(String uri, Collaboration owningCollaboration) {
        super(uri, owningCollaboration);
    }
}
