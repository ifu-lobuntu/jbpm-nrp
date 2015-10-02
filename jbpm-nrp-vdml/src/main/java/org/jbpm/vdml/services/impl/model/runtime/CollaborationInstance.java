package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.*;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class CollaborationInstance extends PortContainerInstance {

    @ManyToOne
    private Collaboration collaboration;

    @OneToMany(mappedBy = "collaboration")
    private Set<ActivityInstance> activities = new HashSet<ActivityInstance>();
    @OneToMany(mappedBy = "collaboration")
    private Set<SupplyingStoreInstance> supplyingStores = new HashSet<SupplyingStoreInstance>();
    @ManyToMany()
    private Set<RolePerformance> collaborationRoles = new HashSet<RolePerformance>();

    @OneToMany(mappedBy = "collaboration")
    private Set<BusinessItemObservation> businessItems = new HashSet<BusinessItemObservation>();
    @OneToMany(mappedBy = "collaboration")
    private Set<DeliverableFlowInstance> ownedDirectedFlows = new HashSet<DeliverableFlowInstance>();

    @OneToMany(mappedBy = "collaboration")
    private Set<MilestoneInstance> milestones = new HashSet<MilestoneInstance>();

    public CollaborationInstance(Collaboration collaboration) {
        this.collaboration = collaboration;
    }

    public CollaborationInstance() {
    }

    public PortContainerInstance findPortContainer(PortContainer pc) {
        if (pc instanceof Activity) {
            return findActivity((Activity) pc);
        } else {
            return findSupplyingStore((SupplyingStore) pc);
        }
    }

    public ActivityInstance findActivity(Activity pc) {
        return findMatchingRuntimeEntity(this.getActivities(), pc);
    }

    public SupplyingStoreInstance findSupplyingStore(SupplyingStore pc) {
        return findMatchingRuntimeEntity(this.getSupplyingStores(), pc);
    }

    public BusinessItemObservation findBusinessItem(BusinessItemDefinition deliverable) {
        return findMatchingRuntimeEntity(this.getBusinessItems(), deliverable);
    }

    @Override
    public Collection<? extends Measurement> getMeasurements() {
        return Collections.emptySet();
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getCollaboration();
    }

    public Collaboration getCollaboration() {
        return collaboration;
    }

    public Set<ActivityInstance> getActivities() {
        return activities;
    }

    public Set<RolePerformance> getCollaborationRoles() {
        return collaborationRoles;
    }

    public Set<BusinessItemObservation> getBusinessItems() {
        return businessItems;
    }

    public Set<DeliverableFlowInstance> getOwnedDirectedFlows() {
        return ownedDirectedFlows;
    }

    public Set<SupplyingStoreInstance> getSupplyingStores() {
        return supplyingStores;
    }

    public RolePerformance findRole(Role role) {
        return findMatchingRuntimeEntity(getCollaborationRoles(), role);
    }

    public Set<MilestoneInstance> getMilestones() {
        return milestones;
    }

    public MilestoneInstance findMilestone(Milestone milestone) {
        return findMatchingRuntimeEntity(getMilestones(), milestone);
    }

    public Collection<CapabilityOffer> getCapabilityOffersUsed() {
        Collection<CapabilityOffer> result = new HashSet<CapabilityOffer>();
        for (ActivityInstance a : this.getActivities()) {
            result.add(a.getCapabilityOffer());
        }
        return result;
    }
    public Collection<StorePerformance> getStoresUsed() {
        Collection<StorePerformance> result = new HashSet<StorePerformance>();
        for (SupplyingStoreInstance a : this.getSupplyingStores()) {
            result.add(a.getStore());
        }
        return result;
    }

    public DeliverableFlowInstance findDeliverableFlow(DeliverableFlow flow) {
        return findMatchingRuntimeEntity(getOwnedDirectedFlows(),flow);
    }

    public SupplyingStoreInstance findSupplyingStore(StoreDefinition definition) {
        for (SupplyingStoreInstance so : getSupplyingStores()) {
            if(so.getSupplyingStore().getStoreRequirement().equals(definition)){
                return so;
            }
        }
        throw new IllegalArgumentException("This collaboration does not have stores of type '" + definition.getName() +"'");
    }
}
