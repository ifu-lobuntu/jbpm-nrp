package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class DirectedFlow implements MetaEntity,MeasurableElement {
    @Id
    protected String uri;
    private String name;
    @ManyToOne
    private Collaboration owningCollaboration;
    private String sourceName;
    private String targetName;
    @ManyToOne
    private Measure quantity;
    @ManyToOne
    private Measure duration;
    @ManyToOne
    private PortContainer sourcePortContainer;
    @ManyToOne
    private PortContainer targetPortContainer;

    @ManyToOne
    private BusinessItemDefinition deliverable;
    @ManyToMany
    private Set<Measure> valueAdds=new HashSet<Measure>();
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

    @Override
    public Collection<Measure> getMeasures() {
        return getValueAdds();
    }

    public Set<Measure> getValueAdds() {
        return valueAdds;
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public PortContainer getSourcePortContainer() {
        return sourcePortContainer;
    }

    public void setSourcePortContainer(PortContainer sourcePortContainer) {
        if(this.sourcePortContainer!=null){
            this.sourcePortContainer.getCommencedFlows().remove(this);
        }
        this.sourcePortContainer = sourcePortContainer;
        if(this.sourcePortContainer!=null){
            this.sourcePortContainer.getCommencedFlows().add(this);
        }

    }

    public PortContainer getTargetPortContainer() {
        return targetPortContainer;
    }

    public void setTargetPortContainer(PortContainer targetPortContainer) {
        if(this.targetPortContainer!=null){
            this.targetPortContainer.getConcludedFlows().remove(this);
        }
        this.targetPortContainer = targetPortContainer;
        if(this.targetPortContainer!=null){
            this.targetPortContainer.getConcludedFlows().add(this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
