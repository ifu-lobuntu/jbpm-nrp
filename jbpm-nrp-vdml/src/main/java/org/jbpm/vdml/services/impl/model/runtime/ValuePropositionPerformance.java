package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ValuePropositionPerformance implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private ValuePropositionPerformance extendedValuePropositionPerformance;
    @ManyToOne
    private ValueProposition valueProposition;
    @Enumerated
    private TrustRelationshipStatus status=TrustRelationshipStatus.REQUESTED;
    @ManyToOne
    private RolePerformance provider;
    @ManyToOne
    private RolePerformance receiver;
    @OneToMany(mappedBy = "valueProposition")
    private Set< ValuePropositionComponentPerformance> components = new HashSet< ValuePropositionComponentPerformance>();

    public ValuePropositionPerformance() {
    }

    public ValuePropositionPerformance(ValueProposition valueProposition, RolePerformance provider) {
        this.valueProposition = valueProposition;
        this.provider = provider;
        this.provider.getProvidedValuePropositions().add(this);
    }

    public TrustRelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(TrustRelationshipStatus status) {
        this.status = status;
    }

    public ValuePropositionPerformance getExtendedValuePropositionPerformance() {
        return extendedValuePropositionPerformance;
    }

    public void setExtendedValuePropositionPerformance(ValuePropositionPerformance extendedValuePropositionPerformance) {
        this.extendedValuePropositionPerformance = extendedValuePropositionPerformance;
    }

    public RolePerformance getProvider() {
        return provider;
    }

    public RolePerformance getReceiver() {
        return receiver;
    }

    public void setReceiver(RolePerformance receiver) {
        this.receiver = receiver;
        this.receiver.getReceivedValuePropositions().add(this);
    }

    public Set< ValuePropositionComponentPerformance> getComponents() {
        return components;
    }

    public ValueProposition getValueProposition() {
        return valueProposition;
    }

    @Override
    public Long getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getValueProposition();
    }
}
