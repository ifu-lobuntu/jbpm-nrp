package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.*;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class CollaborationObservation extends PortContainerObservation {

    @ManyToOne
    private Collaboration collaboration;

    @OneToMany(mappedBy = "collaboration")
    private Set<ActivityObservation> activities = new HashSet<ActivityObservation>();
    @OneToMany(mappedBy = "collaboration")
    private Set<SupplyingStoreObservation> supplyingStores = new HashSet<SupplyingStoreObservation>();
    @ManyToMany()
    private Set<RolePerformance> collaborationRoles = new HashSet<RolePerformance>();

    @OneToMany(mappedBy = "collaboration")
    private Set<BusinessItemObservation> businessItems = new HashSet<BusinessItemObservation>();
    @OneToMany(mappedBy = "collaboration")
    private Set<DirectedFlowObservation> ownedDirectedFlows = new HashSet<DirectedFlowObservation>();

    @OneToMany(mappedBy = "collaboration")
    private Set<MilestoneObservation> milestones = new HashSet<MilestoneObservation>();

    public CollaborationObservation(Collaboration collaboration) {
        this.collaboration = collaboration;
    }

    public CollaborationObservation() {
    }

    public PortContainerObservation findPortContainer(PortContainer pc) {
        if (pc instanceof Activity) {
            return findActivity((Activity) pc);
        } else {
            return findSupplyingStore((SupplyingStore) pc);
        }
    }

    public ActivityObservation findActivity(Activity pc) {
        return findMatchingRuntimeEntity(this.getActivities(), pc);
    }

    public SupplyingStoreObservation findSupplyingStore(SupplyingStore pc) {
        return findMatchingRuntimeEntity(this.getSupplyingStores(), pc);
    }

    public BusinessItemObservation findBusinessItem(BusinessItemDefinition deliverable) {
        return findMatchingRuntimeEntity(this.getBusinessItems(), deliverable);
    }


    @Override
    public MetaEntity getMetaEntity() {
        return getCollaboration();
    }

    public Collaboration getCollaboration() {
        return collaboration;
    }

    public Set<ActivityObservation> getActivities() {
        return activities;
    }

    public Set<RolePerformance> getCollaborationRoles() {
        return collaborationRoles;
    }

    public Set<BusinessItemObservation> getBusinessItems() {
        return businessItems;
    }

    public Set<DirectedFlowObservation> getOwnedDirectedFlows() {
        return ownedDirectedFlows;
    }

    public Set<SupplyingStoreObservation> getSupplyingStores() {
        return supplyingStores;
    }

    public RolePerformance findRole(Role role) {
        return findMatchingRuntimeEntity(getCollaborationRoles(), role);
    }

    public Set<MilestoneObservation> getMilestones() {
        return milestones;
    }

    public MilestoneObservation findMilestone(Milestone milestone) {
        return findMatchingRuntimeEntity(getMilestones(), milestone);
    }

    public Collection<CapabilityPerformance> getCapabilityOffersUsed() {
        Collection<CapabilityPerformance> result = new HashSet<CapabilityPerformance>();
        for (ActivityObservation a : this.getActivities()) {
            result.add(a.getCapabilityOffer());
        }
        return result;
    }
    public Collection<StorePerformance> getStoresUsed() {
        Collection<StorePerformance> result = new HashSet<StorePerformance>();
        for (SupplyingStoreObservation a : this.getSupplyingStores()) {
            result.add(a.getStore());
        }
        return result;
    }
}
