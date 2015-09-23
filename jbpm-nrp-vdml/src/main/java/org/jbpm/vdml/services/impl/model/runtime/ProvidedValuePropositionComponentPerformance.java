package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ProvidedValuePropositionComponentPerformance implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private ProvidedValuePropositionPerformance valueProposition;
    @ManyToOne
    private ValuePropositionComponent valuePropositionComponent;
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    private Set<ProvidedValuePropositionComponentMeasurement> measurements=new HashSet<ProvidedValuePropositionComponentMeasurement>();//Aggregated from related ValueAdds

    public ProvidedValuePropositionComponentPerformance() {
    }

    public ProvidedValuePropositionComponentPerformance(ValuePropositionComponent valuePropositionComponent,ProvidedValuePropositionPerformance valueProposition) {
        this.valueProposition = valueProposition;
        this.valuePropositionComponent = valuePropositionComponent;
        this.valueProposition.getComponents().add(this);
    }

    public ProvidedValuePropositionPerformance getValueProposition() {
        return valueProposition;
    }


    public ValuePropositionComponent getValuePropositionComponent() {
        return valuePropositionComponent;
    }


    public Set<ProvidedValuePropositionComponentMeasurement> getMeasurements() {
        return this.measurements;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getValuePropositionComponent();
    }
}
