package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.Role;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class RolePerformance implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private Role role;
    @ManyToOne
    private Participant participant;
    @OneToMany(mappedBy = "provider",cascade = CascadeType.ALL)
    private Set<ValuePropositionPerformance> providedValuePropositions=new HashSet<ValuePropositionPerformance>();
    @OneToMany(mappedBy = "provider",cascade = CascadeType.ALL)
    private Set<ProvidedValuePropositionPerformance> overallProvidedValuePropositions=new HashSet<ProvidedValuePropositionPerformance>();
    @OneToMany(mappedBy = "receiver")
    private Set<ValuePropositionPerformance> receivedValuePropositions=new HashSet<ValuePropositionPerformance>();

    public RolePerformance() {
    }

    public RolePerformance(Role role, Participant participant) {
        this.role = role;
        this.participant = participant;
        this.participant.getRolePerformances().add(this);
    }

    public Role getRole() {
        return role;
    }

    public Participant getParticipant() {
        return participant;
    }

    public Set<ValuePropositionPerformance> getProvidedValuePropositions() {
        return providedValuePropositions;
    }

    public Set<ValuePropositionPerformance> getReceivedValuePropositions() {
        return receivedValuePropositions;
    }

    public Set<ProvidedValuePropositionPerformance> getOverallProvidedValuePropositions() {
        return overallProvidedValuePropositions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return role;
    }
}
