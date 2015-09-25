package org.jbpm.vdml.services.impl.model.runtime;

import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.meta.Activity;
import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ResourceUse;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class ActivityObservation extends PortContainerObservation {
    @ManyToOne
    private CapabilityPerformance capabilityOffer;
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
    private CollaborationObservation collaboration;
    @OneToMany(mappedBy = "activity")
    private Set<ResourceUseObservation> resourceUseObservation = new HashSet<ResourceUseObservation>();
    @OneToMany(mappedBy = "activity")
    private Set<ActivityMeasurement> measurements = new HashSet<ActivityMeasurement>();

    public ActivityObservation() {
    }

    public ActivityObservation(Activity activity, CollaborationObservation collaboration) {
        super();
        this.activity = activity;
        this.collaboration = collaboration;
        this.collaboration.getActivities().add(this);
    }
    public ResourceUseObservation findResourceUse(ResourceUse ru){
        return findMatchingRuntimeEntity(this.getResourceUseObservation(), ru);

    }

    public CollaborationObservation getCollaboration() {
        return collaboration;
    }

    public void setCapabilityOffer(CapabilityPerformance capabilityOffer) {
        this.capabilityOffer = capabilityOffer;
    }

    public void setPerformingRole(RolePerformance performingRole) {
        setResponsibleRolePerformance(performingRole);
    }

    public Set<ResourceUseObservation> getResourceUseObservation() {
        return resourceUseObservation;
    }

    public RolePerformance getPerformingRole() {
        return getResponsibleRolePerformance();
    }

    public Set<ActivityMeasurement> getMeasurements() {
        return measurements;
    }

    public CapabilityPerformance getCapabilityOffer() {
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
