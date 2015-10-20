package org.jbpm.vdml.services.impl.model.runtime;


import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.meta.DirectedFlow;
import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class DeliverableFlowInstance implements RuntimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private CollaborationInstance collaboration;
    @ManyToOne
    private DirectedFlow directedFlow;
    @ManyToOne
    private MilestoneInstance milestone;
    @ManyToOne
    private PortContainerInstance sourcePortContainer;
    @ManyToOne
    private PortContainerInstance targetPortContainer;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime plannedDate;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime actualDate;
    @Enumerated
    private ValueFlowStatus status=ValueFlowStatus.PLANNING;
    @ManyToOne
    private BusinessItemObservation deliverable;
    @OneToMany(mappedBy = "deliverableFlow")
    private Set<ValueAddMeasurement> valueAddMeasurements=new HashSet<ValueAddMeasurement>();
    @OneToMany(mappedBy = "deliverableFlow")
    private Set<DeliverableFlowMeasurement> measurements=new HashSet<DeliverableFlowMeasurement>();

    public DeliverableFlowInstance() {
    }

    public DeliverableFlowInstance(DirectedFlow directedFlow, CollaborationInstance collaboration) {
        this.collaboration = collaboration;
        this.directedFlow = directedFlow;
        this.collaboration.getOwnedDirectedFlows().add(this);
        sourcePortContainer =collaboration.findPortContainer(directedFlow.getSourcePortContainer());
        sourcePortContainer.getCommencedFlow().add(this);
        targetPortContainer =collaboration.findPortContainer(directedFlow.getTargetPortContainer());
        targetPortContainer.getConcludedFlow().add(this);
        this.deliverable=collaboration.findBusinessItem(directedFlow.getDeliverable());
    }

    public MilestoneInstance getMilestone() {
        return milestone;
    }

    public void setMilestone(MilestoneInstance milestone) {
        this.milestone = milestone;
        milestone.getFlows().add(this);
    }

    public DeliverableFlowInstance(DirectedFlow directedFlow, CollaborationInstance collaboration, PortContainerInstance sourcePortContainer, PortContainerInstance targetPortContainer) {
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

    public CollaborationInstance getCollaboration() {
        return collaboration;
    }

    public DirectedFlow getDirectedFlow() {
        return directedFlow;
    }

    public PortContainerInstance getSourcePortContainer() {
        return sourcePortContainer;
    }

    public PortContainerInstance getTargetPortContainer() {
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

    public Set<DeliverableFlowMeasurement> getMeasurements() {
        return measurements;
    }

    public DeliverableFlowMeasurement getQuantity() {
        return findMatchingRuntimeEntity(getMeasurements(), getDirectedFlow().getQuantity());
    }

    public DateTime getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(DateTime plannedDate) {
        this.plannedDate = plannedDate;
    }

    public DateTime getActualDate() {
        return actualDate;
    }

    public void setActualDate(DateTime actualDate) {
        this.actualDate = actualDate;
    }

    public DeliverableFlowMeasurement findMeasurement(Measure measure) {
        return findMatchingRuntimeEntity(getMeasurements(), measure);
    }

    public ValueAddMeasurement findValueAdd(Measure measure) {
        return findMatchingRuntimeEntity(getValueAddMeasurements(), measure);

    }
}
