package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.*;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntities;
import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class CollaborationInstance extends PortContainerInstance {

    @ManyToOne
    private Collaboration collaboration;

    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<ActivityInstance> activities = new HashSet<ActivityInstance>();
    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<SupplyingStoreInstance> supplyingStores = new HashSet<SupplyingStoreInstance>();
    @ManyToMany()
    private Set<RolePerformance> collaborationRoles = new HashSet<RolePerformance>();

    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<BusinessItemObservation> businessItems = new HashSet<BusinessItemObservation>();
    @OneToMany(mappedBy = "collaboration" ,cascade = CascadeType.ALL)
    private Set<DeliverableFlowInstance> ownedDirectedFlows = new HashSet<DeliverableFlowInstance>();

    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL)
    private Set<MilestoneInstance> milestones = new HashSet<MilestoneInstance>();

    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<ValuePropositionInstance> valuePropositions= new HashSet<ValuePropositionInstance>();

    public CollaborationInstance(Collaboration collaboration) {
        this.collaboration = collaboration;
    }

    public CollaborationInstance() {
    }

    public PortContainerInstance findPortContainer(PortContainer pc) {
        if (pc instanceof Activity) {
            return findFirstActivity((Activity) pc);
        } else {
            return findSupplyingStore((SupplyingStore) pc);
        }
    }

    public Set<ValuePropositionInstance> getValuePropositions() {
        return valuePropositions;
    }

    public ActivityInstance findFirstActivity(Activity pc) {
        return findMatchingRuntimeEntity(this.getActivities(), pc);
    }

    public SupplyingStoreInstance findSupplyingStore(SupplyingStore pc) {
        return findMatchingRuntimeEntity(this.getSupplyingStores(), pc);
    }

    public BusinessItemObservation findFirstBusinessItem(BusinessItemDefinition deliverable) {
        return findMatchingRuntimeEntity(this.getBusinessItems(), deliverable);
    }
    public DeliverableFlowInstance findDeliverableFlow(DeliverableFlow df, BusinessItemObservation bio) {
        for (DeliverableFlowInstance dfi : getOwnedDirectedFlows()) {
            if(dfi.getDeliverableFlow().getUri().equals(df.getUri()) && dfi.getDeliverable().equals(bio)){
                return dfi;
            }
        }
        return null;
    }

    public BusinessItemObservation findBusinessItem(BusinessItemDefinition definition, String externalIdentifier) {
        for (BusinessItemObservation bio : getBusinessItems()) {
            if(bio.getDefinition().getUri().equals(definition.getUri()) && bio.getLocalReference()!=null && bio.getLocalReference().getIdentifier().equals(externalIdentifier)){
                return bio;
            }
        }
        return null;
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

    public DeliverableFlowInstance findFirstDeliverableFlow(DeliverableFlow flow) {
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

    public Collection<? extends PortContainerInstance> findPortContainers(PortContainer pc) {
        if(pc instanceof Activity){
            return findActivities((Activity)pc);
        }else{
            return findSupplyingStores((SupplyingStore) pc);
        }
    }
    public Collection<PortContainerInstance> findResponsibilities(RolePerformance rp, PortContainer pc) {
        return findMatchingRuntimeEntities(findResponsibilities(rp),pc);

    }
    public Collection<PortContainerInstance> findResponsibilities(RolePerformance rp) {
        Set<PortContainerInstance> result = new HashSet<PortContainerInstance>();
        addResponsibilitiesTo(rp, result, getActivities());
        addResponsibilitiesTo(rp, result, getSupplyingStores());
        return result;
    }

    protected void addResponsibilitiesTo(RolePerformance rp, Set<PortContainerInstance> target, Set<? extends PortContainerInstance> source) {
        for (PortContainerInstance pci: source) {
            if(pci.getResponsibleRolePerformance()!=null && pci.getResponsibleRolePerformance().equals(rp)){
                target.add(pci);
            }
        }
    }

    private Collection<SupplyingStoreInstance> findSupplyingStores(SupplyingStore pc) {
        return findMatchingRuntimeEntities(getSupplyingStores(), pc);

    }

    private Collection<ActivityInstance> findActivities(Activity pc) {
        return findMatchingRuntimeEntities(getActivities(), pc);
    }

    public ValuePropositionInstance findValuePropositionInstance(RolePerformance from, RolePerformance to) {
        for (ValuePropositionInstance vpi : getValuePropositions()) {
            if(vpi.getProvider().equals(from) && vpi.getRecipient().equals(to)){
                return vpi;
            }
        }
        return null;
    }
    public Collection<? extends RolePerformance> findRolePerformances(Role role){
        return findMatchingRuntimeEntities(getCollaborationRoles(), role);
    }

}
