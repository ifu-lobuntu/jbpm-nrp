package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class TrustRelationshipComponent implements ActivatableRuntimeEntity,Measurand {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private TrustRelationship relationship;
    @ManyToOne
    private ValuePropositionComponent valuePropositionComponent;
    @OneToMany(mappedBy = "component")
    private Set<TrustRelationshipComponentMeasurement> measurements=new HashSet<TrustRelationshipComponentMeasurement>();//Aggregated from related ValueAddMeasurements

    public TrustRelationshipComponent() {
    }

    public TrustRelationshipComponent(ValuePropositionComponent valuePropositionComponent, TrustRelationship trustRelationship) {
        this.relationship = trustRelationship;
        this.valuePropositionComponent = valuePropositionComponent;
        this.relationship.getComponents().add(this);
    }

    public TrustRelationship getRelationship() {
        return relationship;
    }


    public ValuePropositionComponent getValuePropositionComponent() {
        return valuePropositionComponent;
    }
    public TrustRelationshipComponentMeasurement findMeasurement(Measure measure){
        return findMatchingRuntimeEntity(getMeasurements(),measure);
    }

    public Set<TrustRelationshipComponentMeasurement> getMeasurements() {
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
