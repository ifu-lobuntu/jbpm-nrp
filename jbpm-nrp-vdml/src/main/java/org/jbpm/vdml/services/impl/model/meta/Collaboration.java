package org.jbpm.vdml.services.impl.model.meta;

import com.vividsolutions.jts.geom.Point;
import org.jbpm.vdml.services.impl.model.runtime.Address;
import org.jbpm.vdml.services.impl.model.runtime.MilestoneObservation;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
public class Collaboration extends PortContainer {
    private String deploymentId;
    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<Role> collaborationRoles =new HashSet<Role>();
    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<Activity> activities=new HashSet<Activity>();
    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<SupplyingStore> supplyingStores =new HashSet<SupplyingStore>();
    @ManyToMany(targetEntity = BusinessItemDefinition.class)
    private Set<BusinessItemDefinition> businessItemDefinitions =new HashSet<BusinessItemDefinition>();
    @OneToMany(mappedBy = "owningCollaboration", cascade = CascadeType.ALL)
    private Set<DirectedFlow> ownedDirectedFlows =new HashSet<DirectedFlow>();
    @OneToMany(mappedBy = "collaboration", cascade = CascadeType.ALL)
    private Set<Milestone> milestones =new HashSet<Milestone>();
    @ManyToOne
    private Role initiatorRole;
    @ManyToOne
    private Role plannerRole;
    @ManyToOne

    private Address address;

    public Collaboration() {
    }

    public Collaboration(String uri) {
        super(uri);
    }

    public Set<Activity> getActivities() {
        return activities;
    }

    public Set<DirectedFlow> getFlows() {
        return ownedDirectedFlows;
    }

    public Set<Role> getCollaborationRoles() {
        return collaborationRoles;
    }

    public Set<SupplyingStore> getSupplyingStores() {
        return supplyingStores;
    }

    public Set<BusinessItemDefinition> getBusinessItemDefinitions() {
        return businessItemDefinitions;
    }

    public Role getInitiatorRole() {
        return initiatorRole;
    }

    public void setInitiatorRole(Role initiatorRole) {
        this.initiatorRole = initiatorRole;
    }

    public Role getPlannerRole() {
        return plannerRole;
    }

    public void setPlannerRole(Role plannerRole) {
        this.plannerRole = plannerRole;
    }

    @Override
    public Collection<Measure> getMeasures() {
        return Collections.emptySet();
    }

    public Role findRole(String roleName) {
        return findByName(getCollaborationRoles(), roleName);
    }

    public Activity findActivity(String name) {
        return findByName(getActivities(),name);
    }
    public SupplyingStore findSupplyingStore(String name) {
        return findByName(getSupplyingStores(),name);
    }

    public Set<Milestone> getMilestones() {
        return milestones;
    }

    public Milestone findMilestone(String milestoneName) {
        return findByName(getMilestones(),milestoneName);
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }
}
