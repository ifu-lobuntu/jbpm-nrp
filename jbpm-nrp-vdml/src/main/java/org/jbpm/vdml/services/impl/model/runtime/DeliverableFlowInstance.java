package org.jbpm.vdml.services.impl.model.runtime;


import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.meta.*;
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
    private DeliverableFlow deliverableFlow;
    @ManyToOne
    private MilestoneInstance milestone;
    @ManyToOne
    private PortContainerInstance sourcePortContainer;
    @ManyToOne
    private PortContainerInstance targetPortContainer;
    @ManyToOne
    private OutputPortInstance source;
    @ManyToOne
    private InputPortInstance target;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime plannedDate;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime actualDate;
    @Enumerated
    private ValueFlowStatus status=ValueFlowStatus.PLANNING;
    @ManyToOne
    private BusinessItemObservation deliverable;
    @OneToMany(mappedBy = "deliverableFlow")
    private Set<DeliverableFlowMeasurement> measurements=new HashSet<DeliverableFlowMeasurement>();

    public DeliverableFlowInstance() {
    }

    public DeliverableFlowInstance(DeliverableFlow deliverableFlow, CollaborationInstance collaboration) {
        if(!(deliverableFlow instanceof DeliverableFlow)){
            throw new IllegalArgumentException();
        }
        this.collaboration = collaboration;
        this.deliverableFlow = deliverableFlow;
        this.collaboration.getOwnedDirectedFlows().add(this);
        sourcePortContainer =collaboration.findPortContainer(deliverableFlow.getSourcePortContainer());
        sourcePortContainer.getCommencedFlow().add(this);
        targetPortContainer =collaboration.findPortContainer(deliverableFlow.getTargetPortContainer());
        targetPortContainer.getConcludedFlow().add(this);
        this.deliverable=collaboration.findBusinessItem(deliverableFlow.getDeliverable());
    }

    public MilestoneInstance getMilestone() {
        return milestone;
    }

    public void setMilestone(MilestoneInstance milestone) {
        this.milestone = milestone;
        milestone.getFlows().add(this);
    }

    public DeliverableFlowInstance(DeliverableFlow deliverableFlow, CollaborationInstance collaboration, PortContainerInstance sourcePortContainer, PortContainerInstance targetPortContainer) {
        this.collaboration = collaboration;
        this.deliverableFlow = deliverableFlow;
        this.collaboration.getOwnedDirectedFlows().add(this);
        this.sourcePortContainer =sourcePortContainer;
        this.targetPortContainer =targetPortContainer;
        this.deliverable=collaboration.findBusinessItem(deliverableFlow.getDeliverable());
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
        return getDeliverableFlow();
    }

    public CollaborationInstance getCollaboration() {
        return collaboration;
    }

    public DeliverableFlow getDeliverableFlow() {
        return deliverableFlow;
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
        return getSource().getValueAdds();
    }

    public Set<DeliverableFlowMeasurement> getMeasurements() {
        return measurements;
    }

    public DeliverableFlowMeasurement getQuantity() {
        return findMatchingRuntimeEntity(getMeasurements(), getDeliverableFlow().getQuantity());
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

    public OutputPortInstance getSource() {
        return source;
    }

    public void setSource(OutputPortInstance source) {
        this.source = source;
    }

    public InputPortInstance getTarget() {
        return target;
    }

    public void setTarget(InputPortInstance target) {
        this.target = target;
    }
}
