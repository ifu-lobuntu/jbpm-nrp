package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Port;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("OutputPortInstance")
public class OutputPortInstance extends PortInstance {
    @OneToMany
    private Set<ValueAddMeasurement> valueAdds=new HashSet<ValueAddMeasurement>();
    public OutputPortInstance() {
    }

    public OutputPortInstance(PortContainerInstance portContainer, Port port) {
        super(portContainer, port);
    }

    public Set<ValueAddMeasurement> getValueAdds() {
        return valueAdds;
    }
}
