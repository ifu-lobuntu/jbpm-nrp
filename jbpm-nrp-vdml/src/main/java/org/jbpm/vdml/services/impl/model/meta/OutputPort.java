package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;
import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByType;
import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findOneByType;

@Entity
@DiscriminatorValue("OutputPort")

public class OutputPort extends Port {
    @ManyToMany

    @JoinTable(name = "output_port_value_add")
    private Set<ValueAdd> valueAdds = new HashSet<ValueAdd>();

    public OutputPort(String uri, PortContainer portContainer) {
        super(uri, portContainer);
    }

    public OutputPort() {

    }

    public Set<OutputDelegation> getDelegatedOutputs() {
        return findByType(getInflows(), OutputDelegation.class);
    }

    public DeliverableFlow getOutput() {
        return findOneByType(getOutflows(), DeliverableFlow.class);
    }

    public Set<ValueAdd> getValueAdds() {
        return valueAdds;
    }

    public Set<OutputDelegation> getOutputDelegations() {
        return findByType(getOutflows(), OutputDelegation.class);
    }

    public ValueAdd findValueAdd(String s) {
        return findByName(getValueAdds(), s);
    }
}
