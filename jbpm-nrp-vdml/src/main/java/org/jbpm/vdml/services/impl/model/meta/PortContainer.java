package org.jbpm.vdml.services.impl.model.meta;

import org.jbpm.vdml.services.impl.model.runtime.DeliverableFlowInstance;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;
import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByType;

@Entity()
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PortContainer implements MetaEntity, MeasurableElement {
    @Id
    private String uri;
    private String name;

    @OneToMany(mappedBy = "portContainer",cascade = CascadeType.ALL)
    private Set<Port> containedPorts = new HashSet<Port>();
    @OneToMany(mappedBy = "portContainer", cascade = CascadeType.ALL)
    private Set<RoleResource> roleResources=new HashSet<RoleResource>();

    public PortContainer(String uri) {
        this.uri = uri;
    }


    protected PortContainer() {
    }

    public Set<Port> getContainedPorts() {
        return containedPorts;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUri() {
        return uri;
    }

    protected Set<DeliverableFlow> getDeliverableFlowsFrom(Set<DirectedFlow> source) {
        Set<DeliverableFlow> result = new HashSet<DeliverableFlow>();
        for (DirectedFlow flow : source) {
            if (flow instanceof DeliverableFlow) {
                result.add((DeliverableFlow) flow);
            }
        }
        return result;
    }
    public Set<InputPort> getInput(){
        return findByType(getContainedPorts(), InputPort.class);
    }
    public Set<OutputPort> getOutput(){
        return findByType(getContainedPorts(), OutputPort.class);
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }


    public InputPort findInputPort(String inputName) {
        return findByName(getInput(),inputName);
    }

    public OutputPort findOutputPort(String name) {
        return findByName(getOutput(), name);
    }

    public Collection<DeliverableFlow> getOutputFlows() {
        Set<DeliverableFlow> result=new HashSet<DeliverableFlow>();
        for (Port port : containedPorts) {
            if(port instanceof OutputPort && ((OutputPort) port).getOutput()!=null){
                result.add(((OutputPort) port).getOutput());
            }
        }
        return result;
    }
    public Collection<DeliverableFlow> getInputFlows() {
        Set<DeliverableFlow> result=new HashSet<DeliverableFlow>();
        for (Port port : containedPorts) {
            if(port instanceof InputPort && ((InputPort) port).getInput()!=null){
                result.add(((InputPort) port).getInput());
            }
        }
        return result;
    }

    public Set<RoleResource> getRoleResources() {
        return roleResources;
    }
}
