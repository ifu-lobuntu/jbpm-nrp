package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Entity
public class ValuePropositionComponentPerformance implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private ValuePropositionPerformance valueProposition;
    @ManyToOne
    private ValuePropositionComponent valuePropositionComponent;
    @OneToMany(mappedBy = "component")
    private Set< ValuePropositionComponentMeasurement> measurements=new HashSet<ValuePropositionComponentMeasurement>();//Aggregated from related ValueAddMeasurements

    public ValuePropositionComponentPerformance() {
    }

    public ValuePropositionComponentPerformance(ValuePropositionComponent valuePropositionComponent,ValuePropositionPerformance valuePropositionPerformance) {
        this.valueProposition = valuePropositionPerformance;
        this.valuePropositionComponent = valuePropositionComponent;
        this.valueProposition.getComponents().add(this);
    }

    public ValuePropositionPerformance getValueProposition() {
        return valueProposition;
    }


    public ValuePropositionComponent getValuePropositionComponent() {
        return valuePropositionComponent;
    }


    public Set< ValuePropositionComponentMeasurement> getMeasurements() {
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
