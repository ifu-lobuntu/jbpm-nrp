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

    /**
     * This is the constructor used when a collaboration is started. It assumes single instances of each activity and businessitem
     */
    public DeliverableFlowInstance(DeliverableFlow deliverableFlow, CollaborationInstance collaboration) {
        init(deliverableFlow, collaboration);
        linkSource(collaboration.findPortContainer(this.deliverableFlow.getSourcePortContainer()));
        linkTarget(collaboration.findPortContainer(this.deliverableFlow.getTargetPortContainer()));
        this.deliverable=collaboration.findFirstBusinessItem(this.deliverableFlow.getDeliverable());
    }

    /**
     * This is the constructor used when a new instance of the activity is created. It assumes multiple instances of the business item.
     */
    public DeliverableFlowInstance(CollaborationInstance collaboration, BusinessItemObservation bio, InputPortInstance target) {
        init(target.getPort().getInput(), collaboration);
        linkSource(collaboration.findPortContainer(this.deliverableFlow.getSourcePortContainer()));
        linkTarget(target.getPortContainer());
        this.deliverable=bio;
    }


    public DeliverableFlowInstance(CollaborationInstance collaboration, InputPortInstance target) {
        init(target.getPort().getInput(), collaboration);
        linkSource(collaboration.findPortContainer(this.deliverableFlow.getSourcePortContainer()));
        linkTarget(target.getPortContainer());
        linkToExistingBusinessItem(getSource().getOutflow());
    }
    public DeliverableFlowInstance(CollaborationInstance collaboration, OutputPortInstance source,BusinessItemObservation bio) {
        init(source.getPort().getOutput(), collaboration);
        linkSource(source.getPortContainer());
        linkTarget(collaboration.findPortContainer(this.deliverableFlow.getTargetPortContainer()));
        setDeliverable(bio);
    }

    private void linkToExistingBusinessItem(Set<DeliverableFlowInstance> peerFlows) {
        for (DeliverableFlowInstance f : peerFlows) {
            if(f.getDeliverable()!=null){
                setDeliverable(f.getDeliverable());
                break;
            }
        }
    }

    private void linkTarget(PortContainerInstance targetPortContainer) {
        this.targetPortContainer = targetPortContainer;
        this.targetPortContainer.getConcludedFlow().add(this);
        this.target=targetPortContainer.findInputPort(this.deliverableFlow.getTarget());
        this.target.getInflow().add(this);
    }

    private void linkSource(PortContainerInstance sourcePortContainer) {
        this.sourcePortContainer = sourcePortContainer;
        this.sourcePortContainer.getCommencedFlow().add(this);
        this.source =sourcePortContainer.findOutputPort(this.deliverableFlow.getSource());
        this.source.getOutflow().add(this);
    }

    private void init(DeliverableFlow deliverableFlow, CollaborationInstance collaboration) {
        this.collaboration = collaboration;
        this.deliverableFlow = deliverableFlow;
        this.collaboration.getOwnedDirectedFlows().add(this);
    }

    public MilestoneInstance getMilestone() {
        return milestone;
    }

    public void setMilestone(MilestoneInstance milestone) {
        this.milestone = milestone;
        milestone.getFlows().add(this);
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


    public Set<DeliverableFlowMeasurement> getMeasurements() {
        return measurements;
    }

    public PortMeasurement getQuantity() {
        if(getSource().getPort().getBatchSize()!=null){
            return getSource().getBatchSize();
        }else if(getTarget().getPort().getBatchSize()!=null){
            return getTarget().getBatchSize();
        }
        return null;
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
