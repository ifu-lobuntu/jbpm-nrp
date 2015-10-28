package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity()
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PortContainer implements MetaEntity, MeasurableElement {
    @Id
    private String uri;
    private String name;

    @OneToMany(mappedBy = "portContainer",cascade = CascadeType.ALL)
    private Set<InputPort> input= new HashSet<InputPort>();
    @OneToMany(mappedBy = "portContainer",cascade = CascadeType.ALL)
    private Set<OutputPort> output= new HashSet<OutputPort>();

    public PortContainer(String uri) {
        this.uri = uri;
    }


    protected PortContainer() {
    }

    public Set<InputPort> getInput() {
        return input;
    }

    public Set<OutputPort> getOutput() {
        return output;
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

    @Override
    public void setName(String name) {
        this.name = name;
    }


}
