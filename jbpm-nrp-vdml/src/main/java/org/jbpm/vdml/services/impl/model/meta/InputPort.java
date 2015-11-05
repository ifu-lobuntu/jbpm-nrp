package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByType;
import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findOneByType;

@Entity
@DiscriminatorValue("InputPort")

public class InputPort extends Port {


    public InputPort(String uri, PortContainer portContainer) {
        super(uri, portContainer);
    }

    public InputPort() {

    }

    public Set<InputDelegation> getDelegatedInputs() {
        return findByType(super.getInflows(), InputDelegation.class);
    }

    public DeliverableFlow getInput() {
        return findOneByType(getInflows(), DeliverableFlow.class);
    }

    public Set<InputDelegation> getInputDelegations() {
        return findByType(super.getOutflows(), InputDelegation.class);
    }


}
