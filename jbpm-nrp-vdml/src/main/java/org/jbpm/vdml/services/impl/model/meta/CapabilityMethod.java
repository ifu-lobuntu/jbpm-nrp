package org.jbpm.vdml.services.impl.model.meta;

import org.jbpm.vdml.services.impl.model.runtime.RolePerformance;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;
import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntities;

@Entity
@DiscriminatorValue("CapabilityMethod")
public class CapabilityMethod extends Collaboration {
    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<Activity> activities=new HashSet<Activity>();
    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<SupplyingStore> supplyingStores =new HashSet<SupplyingStore>();
    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL)
    private Set<Milestone> milestones =new HashSet<Milestone>();
    @ManyToOne
    private RoleInCapabilityMethod initiatorRole;
    @ManyToOne
    private RoleInCapabilityMethod plannerRole;

    public CapabilityMethod() {
    }

    public CapabilityMethod(String uri) {
        super(uri);
    }

    public Set<Activity> getActivities() {
        return activities;
    }

    public Set<SupplyingStore> getSupplyingStores() {
        return supplyingStores;
    }

    public RoleInCapabilityMethod getInitiatorRole() {
        return initiatorRole;
    }

    public void setInitiatorRole(RoleInCapabilityMethod initiatorRole) {
        this.initiatorRole = initiatorRole;
    }

    public RoleInCapabilityMethod getPlannerRole() {
        return plannerRole;
    }

    public void setPlannerRole(RoleInCapabilityMethod plannerRole) {
        this.plannerRole = plannerRole;
    }

    public Activity findActivity(String name) {
        return findByName(getActivities(),name);
    }

    public SupplyingStore findSupplyingStore(String name) {
        return findByName(getSupplyingStores(),name);
    }
    public RoleInCapabilityMethod findRole(String roleName) {
        return (RoleInCapabilityMethod) findByName(getCollaborationRoles(), roleName);
    }

    public Set<Milestone> getMilestones() {
        return milestones;
    }

    public Milestone findMilestone(String milestoneName) {
        return findByName(getMilestones(),milestoneName);
    }
}
