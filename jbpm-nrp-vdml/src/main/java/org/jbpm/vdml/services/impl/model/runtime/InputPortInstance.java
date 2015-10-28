package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Port;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("InputPortInstance")
public class InputPortInstance extends PortInstance{
    public InputPortInstance() {
    }

    public InputPortInstance(PortContainerInstance portContainer, Port port) {
        super(portContainer, port);
    }
}
