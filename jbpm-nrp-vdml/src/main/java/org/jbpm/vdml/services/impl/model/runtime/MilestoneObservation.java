package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.Milestone;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class MilestoneObservation implements  RuntimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date registeredOn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date commitedOn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date occurredOn;
    @ManyToOne
    private Milestone milestone;
    @ManyToOne
    private CollaborationObservation collaboration;
    @OneToMany(mappedBy = "milestone")
    private Set<DirectedFlowObservation> flows = new HashSet<DirectedFlowObservation>();

    public MilestoneObservation(Milestone milestone, CollaborationObservation collaboration) {
        this.milestone = milestone;
        this.collaboration = collaboration;
        this.collaboration.getMilestones().add(this);
        this.registeredOn=new Date();
    }

    public MilestoneObservation() {
    }
    public void occur(){
        this.occurredOn=new Date();
    }
    public void commit(){
        this.commitedOn=new Date();
    }

    public Date getOccurredOn() {
        return occurredOn;
    }

    public Date getRegisteredOn() {
        return registeredOn;
    }

    @Override
    public Long getId() {
        return id;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public CollaborationObservation getCollaboration() {
        return collaboration;
    }

    public Set<DirectedFlowObservation> getFlows() {
        return flows;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getMilestone();
    }
}
