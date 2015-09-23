package org.jbpm.vdml.services.impl.model.meta;


import javax.persistence.*;

@Entity
@DiscriminatorValue("OutputDelegation")
public class OutputDelegation extends DirectedFlow {

    public OutputDelegation() {
    }

    public OutputDelegation(String uri, Collaboration owningCollaboration) {
        super(uri, owningCollaboration);
    }


}
