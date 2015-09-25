package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class ValuePropositionPerformance implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private ValueProposition valueProposition;
    @ManyToOne
    private RolePerformance provider;

    @OneToMany(mappedBy = "valueProposition", cascade = CascadeType.ALL)
    private Set<ValuePropositionComponentPerformance> components = new HashSet<ValuePropositionComponentPerformance>();

    public ValuePropositionPerformance() {
    }

    public ValuePropositionPerformance(ValueProposition valueProposition, RolePerformance provider) {
        this.valueProposition = valueProposition;
        this.provider = provider;
        this.provider.getProvidedValuePropositions().add(this);
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

    public Set<ValuePropositionComponentPerformance> getComponents() {
        return components;
    }

    public ValueProposition getValueProposition() {
        return valueProposition;
    }

    public ValuePropositionComponentPerformance findComponent(ValuePropositionComponent component) {
        return findMatchingRuntimeEntity(getComponents(),component);
    }
}
