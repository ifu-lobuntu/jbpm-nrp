package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Activity;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ResourceUse;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class ActivityObservation extends PortContainerObservation {
    @ManyToOne
    private CapabilityPerformance capabilityOffer;
    @ManyToOne
    private Activity activity;
    @ManyToOne
    private RolePerformance performingRole;
    @Temporal(TemporalType.TIMESTAMP)
    private Date plannedStartDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date plannedDateOfCompletion;
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualStartDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualDateOfCompletion;
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
        this.performingRole = performingRole;
    }

    public Set<ResourceUseObservation> getResourceUseObservation() {
        return resourceUseObservation;
    }

    public RolePerformance getPerformingRole() {
        return performingRole;
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

    public Date getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(Date plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public Date getPlannedDateOfCompletion() {
        return plannedDateOfCompletion;
    }

    public void setPlannedDateOfCompletion(Date plannedDateOfCompletion) {
        this.plannedDateOfCompletion = plannedDateOfCompletion;
    }

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualDateOfCompletion() {
        return actualDateOfCompletion;
    }

    public void setActualDateOfCompletion(Date actualDateOfCompletion) {
        this.actualDateOfCompletion = actualDateOfCompletion;
    }
}
