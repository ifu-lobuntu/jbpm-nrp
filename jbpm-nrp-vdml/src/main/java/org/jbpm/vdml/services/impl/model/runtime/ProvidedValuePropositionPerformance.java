package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ProvidedValuePropositionPerformance implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private ProvidedValuePropositionPerformance extendedValuePropositionPerformance;
    @ManyToOne
    private ValueProposition valueProposition;
    @ManyToOne
    private RolePerformance provider;

    @OneToMany(mappedBy = "valueProposition", cascade = CascadeType.ALL)
    private Set<ProvidedValuePropositionComponentPerformance> components = new HashSet<ProvidedValuePropositionComponentPerformance>();

    public ProvidedValuePropositionPerformance() {
    }

    public ProvidedValuePropositionPerformance(ValueProposition valueProposition, RolePerformance provider) {
        this.valueProposition = valueProposition;
        this.provider = provider;
        this.provider.getOverallProvidedValuePropositions().add(this);
    }

    public ProvidedValuePropositionPerformance getExtendedValuePropositionPerformance() {
        return extendedValuePropositionPerformance;
    }

    public void setExtendedValuePropositionPerformance(ProvidedValuePropositionPerformance extendedValuePropositionPerformance) {
        this.extendedValuePropositionPerformance = extendedValuePropositionPerformance;
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
    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getValueProposition();
    }

    public RolePerformance getProvider() {
        return provider;
    }

    public Set<ProvidedValuePropositionComponentPerformance> getComponents() {
        return components;
    }

    public ValueProposition getValueProposition() {
        return valueProposition;
    }
}
