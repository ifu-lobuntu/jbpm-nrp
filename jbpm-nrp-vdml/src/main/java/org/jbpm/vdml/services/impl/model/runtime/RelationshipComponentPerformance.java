package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class RelationshipComponentPerformance implements ActivatableRuntimeEntity,Measurand {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private RelationshipPerformance relationship;
    @ManyToOne
    private ValuePropositionComponent valuePropositionComponent;
    @OneToMany(mappedBy = "component")
    private Set<RelationshipComponentMeasurement> measurements=new HashSet<RelationshipComponentMeasurement>();//Aggregated from related ValueAddMeasurements

    public RelationshipComponentPerformance() {
    }

    public RelationshipComponentPerformance(ValuePropositionComponent valuePropositionComponent, RelationshipPerformance relationshipPerformance) {
        this.relationship = relationshipPerformance;
        this.valuePropositionComponent = valuePropositionComponent;
        this.relationship.getComponents().add(this);
    }

    public RelationshipPerformance getRelationship() {
        return relationship;
    }


    public ValuePropositionComponent getValuePropositionComponent() {
        return valuePropositionComponent;
    }
    public RelationshipComponentMeasurement findMeasurement(Measure measure){
        return findMatchingRuntimeEntity(getMeasurements(),measure);
    }

    public Set<RelationshipComponentMeasurement> getMeasurements() {
        return measurements;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getValuePropositionComponent();
    }
}
