package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class RelationshipPerformance implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private ValueProposition valueProposition;
    @Enumerated
    private TrustRelationshipStatus status=TrustRelationshipStatus.REQUESTED;
    @ManyToOne
    private RolePerformance provider;
    @ManyToOne
    private RolePerformance recipient;
    @OneToMany(mappedBy = "relationship")
    private Set<RelationshipComponentPerformance> components = new HashSet<RelationshipComponentPerformance>();

    public RelationshipPerformance() {
    }

    public RelationshipPerformance(ValueProposition valueProposition, RolePerformance provider) {
        this.valueProposition = valueProposition;
        this.provider = provider;
        this.provider.getProvidedRelationships().add(this);
    }

    public TrustRelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(TrustRelationshipStatus status) {
        this.status = status;
    }

    public RolePerformance getProvider() {
        return provider;
    }

    public RolePerformance getRecipient() {
        return recipient;
    }

    public void setRecipient(RolePerformance receiver) {
        this.recipient = receiver;
        this.recipient.getReceivedRelationships().add(this);
    }
    public RelationshipComponentPerformance findComponent(ValuePropositionComponent component){
        return findMatchingRuntimeEntity(getComponents(),component);
    }
    public Set<RelationshipComponentPerformance> getComponents() {
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
