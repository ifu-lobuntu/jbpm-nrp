package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class CollaborationPerformance extends Participant implements RuntimeEntity{

    @ManyToOne
    private Collaboration collaboration;


    @OneToMany(mappedBy = "collaboration")
    private Set<ActivityInstance> activities = new HashSet<ActivityInstance>();
    @OneToMany(mappedBy = "collaboration")
    private Set<SupplyingStoreInstance> suppliedStores = new HashSet<SupplyingStoreInstance>();
    @ManyToMany()
    private Set<RolePerformance> rolePerformances = new HashSet<RolePerformance>();

    @OneToMany(mappedBy = "collaboration")
    private Set<BusinessItemObservation> businessItemObservations = new HashSet<BusinessItemObservation>();
    @OneToMany(mappedBy = "collaboration")
    private Set<DeliverableFlowInstance> deliverableFlowInstances = new HashSet<DeliverableFlowInstance>();
    @OneToMany(mappedBy = "toPortContainer")
    private Set<DeliverableFlowInstance> observedInput = new HashSet<DeliverableFlowInstance>();
    @OneToMany(mappedBy = "fromPortContainer")
    private Set<DeliverableFlowInstance> observedOutput = new HashSet<DeliverableFlowInstance>();


    public CollaborationPerformance(Collaboration collaboration) {
        this.collaboration = collaboration;
    }

    public CollaborationPerformance() {
    }

    public Set<DeliverableFlowInstance> getObservedInput() {
        return observedInput;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getCollaboration();
    }

    public Set<DeliverableFlowInstance> getObservedOutput() {
        return observedOutput;
    }

    public Collaboration getCollaboration() {
        return collaboration;
    }

    public Set<ActivityInstance> getActivities() {
        return activities;
    }

    public Set<RolePerformance> getRolePerformances() {
        return rolePerformances;
    }

    public Set<BusinessItemObservation> getBusinessItemObservations() {
        return businessItemObservations;
    }

    public Set<DeliverableFlowInstance> getDeliverableFlowInstances() {
        return deliverableFlowInstances;
    }

    public Set<SupplyingStoreInstance> getSuppliedStores() {
        return suppliedStores;
    }
}
