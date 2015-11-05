package org.jbpm.vdml.services.impl.model.meta;

import org.jbpm.vdml.services.impl.model.runtime.ExchangeConfiguration;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
@DiscriminatorValue("ValueAdd")
public class ValueAdd extends ValueElement{
    @ManyToOne
    private OutputPort outputPort;
    public ValueAdd() {

    }

    public ValueAdd(String uri, OutputPort op) {
        super(uri);
        this.outputPort=op;
        this.outputPort.getValueAdds().add(this);
    }

    public OutputPort getOutputPort() {
        return outputPort;
    }
}
