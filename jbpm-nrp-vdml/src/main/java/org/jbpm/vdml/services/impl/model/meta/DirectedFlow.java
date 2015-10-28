package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class DirectedFlow implements MetaEntity,MeasurableElement {
    @Id
    protected String uri;
    private String name;
    @ManyToOne
    private Collaboration owningCollaboration;
    @ManyToOne
    private Measure quantity;
    @ManyToOne
    private Measure duration;
    @ManyToOne
    private Port source;
    @ManyToOne
    private Port target;

    @ManyToOne
    private BusinessItemDefinition deliverable;
    @ManyToMany
    private Set<Measure> measures=new HashSet<Measure>();
    public DirectedFlow() {
    }

    public DirectedFlow(String uri, Collaboration owningCollaboration) {
        this.uri = uri;
        this.owningCollaboration = owningCollaboration;
        this.getOwningCollaboration().getFlows().add(this);
    }

    public Collaboration getOwningCollaboration() {
        return owningCollaboration;
    }


    public Set<Measure> getMeasures() {
        return measures;
    }

    public Measure getDuration() {
        return duration;
    }

    public void setDuration(Measure duration) {
        this.duration = duration;
    }

    public Measure getQuantity() {
        return quantity;
    }

    public void setQuantity(Measure quantity) {
        this.quantity = quantity;
    }

    public BusinessItemDefinition getDeliverable() {
        return deliverable;
    }

    public void setDeliverable(BusinessItemDefinition deliverable) {
        this.deliverable = deliverable;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public PortContainer getSourcePortContainer() {
        return source.getPortContainer();
    }

    public PortContainer getTargetPortContainer() {
        return target.getPortContainer();
    }

    public Port getSource() {
        return source;
    }

    public Port getTarget() {
        return target;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Measure findMeasure(String name) {
        return findByName(getMeasures(), name);
    }

    public void setTarget(Port target) {
        if(this.target!=null){
            this.target.getInflows().remove(this);
        }
        this.target = target;

        if(this.target!=null){
            this.target.getInflows().add(this);
        }
    }

    public void setSource(Port source) {
        if(this.source!=null){
            this.source.getOutflows().remove(this);
        }
        this.source = source;

        if(this.source!=null){
            this.source.getOutflows().add(this);
        }
    }
}
