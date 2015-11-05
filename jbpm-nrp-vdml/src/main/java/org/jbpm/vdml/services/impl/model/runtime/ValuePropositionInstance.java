package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class ValuePropositionInstance implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active=true;
    @ManyToOne
    private ValueProposition valueProposition;
    @ManyToOne
    private CollaborationInstance collaboration;
    @ManyToOne
    private RolePerformance provider;
    @ManyToOne
    private RolePerformance recipient;

    @OneToMany(mappedBy = "valueProposition", cascade = CascadeType.ALL)
    private Set<ValuePropositionComponentInstance> components = new HashSet<ValuePropositionComponentInstance>();
    @OneToMany(mappedBy = "valueProposition")
    private Set<OutputPortInstance> outputPorts= new HashSet<OutputPortInstance>();

    public ValuePropositionInstance() {
    }

    public ValuePropositionInstance(CollaborationInstance collaboration, ValueProposition valueProposition, RolePerformance provider,RolePerformance recipient) {
        this.valueProposition = valueProposition;
        this.provider = provider;
        this.recipient=recipient;
        this.collaboration=collaboration;
        this.collaboration.getValuePropositions().add(this);
    }

    public RolePerformance getRecipient() {
        return recipient;
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

    public Set<ValuePropositionComponentInstance> getComponents() {
        return components;
    }

    public ValueProposition getValueProposition() {
        return valueProposition;
    }

    public ValuePropositionComponentInstance findComponent(ValuePropositionComponent component) {
        return findMatchingRuntimeEntity(getComponents(),component);
    }

    public Set<OutputPortInstance> getOutputPorts() {
        return outputPorts;
    }
}
