package org.jbpm.vdml.services.impl.model.runtime;

import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.meta.*;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class ActivityInstance extends PortContainerInstance {
    @ManyToOne
    private CapabilityOffer capabilityOffer;
    @ManyToOne
    private Activity activity;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime plannedStartDate;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime plannedDateOfCompletion;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime actualStartDate;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime actualDateOfCompletion;
    @ManyToOne
    private CollaborationInstance collaboration;
    @OneToMany(mappedBy = "activity")
    private Set<ResourceUseInstance> resourceUseInstance = new HashSet<ResourceUseInstance>();
    @OneToMany(mappedBy = "activity")
    private Set<ActivityMeasurement> measurements = new HashSet<ActivityMeasurement>();

    public ActivityInstance() {
    }

    @Override
    public PortContainer getPortContainer() {
        return activity;
    }

    @Override
    public RoleInCapabilityMethod getResponsibleRole() {
        return getActivity().getPerformingRole();
    }

    public ActivityInstance(Activity activity, CollaborationInstance collaboration) {
        super();
        this.activity = activity;
        this.collaboration = collaboration;
        this.collaboration.getActivities().add(this);
    }
    public ResourceUseInstance findResourceUse(ResourceUse ru){
        return findMatchingRuntimeEntity(this.getResourceUseInstance(), ru);

    }

    public CollaborationInstance getCollaboration() {
        return collaboration;
    }

    public void setCapabilityOffer(CapabilityOffer capabilityOffer) {
        this.capabilityOffer = capabilityOffer;
    }

    public void setPerformingRole(RolePerformance performingRole) {
        setResponsibleRolePerformance(performingRole);
    }

    public Set<ResourceUseInstance> getResourceUseInstance() {
        return resourceUseInstance;
    }

    public RolePerformance getPerformingRole() {
        return getResponsibleRolePerformance();
    }
    public ActivityMeasurement getDuration(){
        return findMatchingRuntimeEntity(getMeasurements(),getActivity().getDuration());
    }

    public Set<ActivityMeasurement> getMeasurements() {
        return measurements;
    }

    public CapabilityOffer getCapabilityOffer() {
        return capabilityOffer;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getActivity();
    }

    public Activity getActivity() {
        return activity;
    }

    public DateTime getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(DateTime plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public DateTime getPlannedDateOfCompletion() {
        return plannedDateOfCompletion;
    }

    public void setPlannedDateOfCompletion(DateTime plannedDateOfCompletion) {
        this.plannedDateOfCompletion = plannedDateOfCompletion;
    }

    public DateTime getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(DateTime actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public DateTime getActualDateOfCompletion() {
        return actualDateOfCompletion;
    }

    public void setActualDateOfCompletion(DateTime actualDateOfCompletion) {
        this.actualDateOfCompletion = actualDateOfCompletion;
    }

    public ActivityMeasurement findMeasurement(Measure measure) {
        return findMatchingRuntimeEntity(getMeasurements(),measure);
    }
}
