package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.InputPort;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("InputPortInstance")
public class InputPortInstance extends PortInstance{
    public InputPortInstance() {
    }
    public InputPort getPort(){
        return (InputPort) super.getPort();
    }
    public InputPortInstance(InputPort port, PortContainerInstance portContainer) {
        super(port, portContainer);
    }
    public DeliverableFlowInstance getInput(){
        if(getInflow().size()>1){
            throw new IllegalStateException("Port '" + getPort().getName() +"' has more that one input DeliverableFlow");
        }
        return getInflow().iterator().next();
    }

}
