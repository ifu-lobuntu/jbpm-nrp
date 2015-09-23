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
    private Set<ActivityObservation> activities = new HashSet<ActivityObservation>();
    @OneToMany(mappedBy = "collaboration")
    private Set<SupplyingStoreObservation> suppliedStores = new HashSet<SupplyingStoreObservation>();
    @ManyToMany()
    private Set<RolePerformance> rolePerformances = new HashSet<RolePerformance>();

    @OneToMany(mappedBy = "collaboration")
    private Set<BusinessItemObservation> businessItemObservations = new HashSet<BusinessItemObservation>();
    @OneToMany(mappedBy = "collaboration")
    private Set<DirectedFlowObservation> directedFlowObservations = new HashSet<DirectedFlowObservation>();
    @OneToMany(mappedBy = "toPortContainer")
    private Set<DirectedFlowObservation> observedInput = new HashSet<DirectedFlowObservation>();
    @OneToMany(mappedBy = "fromPortContainer")
    private Set<DirectedFlowObservation> observedOutput = new HashSet<DirectedFlowObservation>();


    public CollaborationPerformance(Collaboration collaboration) {
        this.collaboration = collaboration;
    }

    public CollaborationPerformance() {
    }

    public Set<DirectedFlowObservation> getObservedInput() {
        return observedInput;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getCollaboration();
    }

    public Set<DirectedFlowObservation> getObservedOutput() {
        return observedOutput;
    }

    public Collaboration getCollaboration() {
        return collaboration;
    }

    public Set<ActivityObservation> getActivities() {
        return activities;
    }

    public Set<RolePerformance> getRolePerformances() {
        return rolePerformances;
    }

    public Set<BusinessItemObservation> getBusinessItemObservations() {
        return businessItemObservations;
    }

    public Set<DirectedFlowObservation> getDirectedFlowObservations() {
        return directedFlowObservations;
    }

    public Set<SupplyingStoreObservation> getSuppliedStores() {
        return suppliedStores;
    }
}
