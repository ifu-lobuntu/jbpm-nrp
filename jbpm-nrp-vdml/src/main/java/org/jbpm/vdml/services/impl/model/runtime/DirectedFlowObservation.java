package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.DirectedFlow;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class DirectedFlowObservation implements RuntimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private CollaborationObservation collaboration;
    @ManyToOne
    private DirectedFlow directedFlow;
    @ManyToOne
    private MilestoneObservation milestone;
    @ManyToOne
    private PortContainerObservation sourcePortContainer;
    @ManyToOne
    private PortContainerObservation targetPortContainer;
    @Temporal(TemporalType.TIMESTAMP)
    private Date plannedDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualDate;
    @Enumerated
    private ValueFlowStatus status=ValueFlowStatus.PLANNING;
    @ManyToOne
    private BusinessItemObservation deliverable;
    @OneToMany(mappedBy = "deliverableFlow")
    private Set<ValueAddMeasurement> valueAddMeasurements=new HashSet<ValueAddMeasurement>();
    @OneToMany(mappedBy = "directedFlow")
    private Set<DirectedFlowMeasurement> measurements=new HashSet<DirectedFlowMeasurement>();

    public DirectedFlowObservation() {
    }

    public DirectedFlowObservation(DirectedFlow directedFlow,CollaborationObservation collaboration) {
        this.collaboration = collaboration;
        this.directedFlow = directedFlow;
        this.collaboration.getOwnedDirectedFlows().add(this);
        sourcePortContainer =collaboration.findPortContainer(directedFlow.getSourcePortContainer());
        sourcePortContainer.getCommencedFlow().add(this);
        targetPortContainer =collaboration.findPortContainer(directedFlow.getTargetPortContainer());
        targetPortContainer.getConcludedFlow().add(this);
        this.deliverable=collaboration.findBusinessItem(directedFlow.getDeliverable());
    }

    public MilestoneObservation getMilestone() {
        return milestone;
    }

    public void setMilestone(MilestoneObservation milestone) {
        this.milestone = milestone;
        milestone.getFlows().add(this);
    }

    public DirectedFlowObservation(DirectedFlow directedFlow, CollaborationObservation collaboration, PortContainerObservation sourcePortContainer, PortContainerObservation targetPortContainer) {
        this.collaboration = collaboration;
        this.directedFlow = directedFlow;
        this.collaboration.getOwnedDirectedFlows().add(this);
        this.sourcePortContainer =sourcePortContainer;
        this.targetPortContainer =targetPortContainer;
        this.deliverable=collaboration.findBusinessItem(directedFlow.getDeliverable());
    }

    public Long getId() {
        return id;
    }

    public ValueFlowStatus getStatus() {
        return status;
    }

    public void setStatus(ValueFlowStatus status) {
        this.status = status;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getDirectedFlow();
    }

    public CollaborationObservation getCollaboration() {
        return collaboration;
    }

    public DirectedFlow getDirectedFlow() {
        return directedFlow;
    }

    public PortContainerObservation getSourcePortContainer() {
        return sourcePortContainer;
    }

    public PortContainerObservation getTargetPortContainer() {
        return targetPortContainer;
    }

    public void setDeliverable(BusinessItemObservation deliverable) {
        this.deliverable = deliverable;
    }

    public BusinessItemObservation getDeliverable() {
        return deliverable;
    }

    public Set<ValueAddMeasurement> getValueAddMeasurements() {
        return valueAddMeasurements;
    }

    public Set<DirectedFlowMeasurement> getMeasurements() {
        return measurements;
    }

    public DirectedFlowMeasurement getQuantity() {
        for (DirectedFlowMeasurement measurement : measurements) {
            if(measurement.getMeasure().equals(directedFlow.getQuantity())){
                return measurement;
            }
        }
        return null;
    }

    public Date getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(Date plannedDate) {
        this.plannedDate = plannedDate;
    }

    public Date getActualDate() {
        return actualDate;
    }

    public void setActualDate(Date actualDate) {
        this.actualDate = actualDate;
    }
}
