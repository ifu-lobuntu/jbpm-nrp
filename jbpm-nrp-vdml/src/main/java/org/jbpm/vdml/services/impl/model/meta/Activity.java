package org.jbpm.vdml.services.impl.model.meta;

import com.google.common.collect.UnmodifiableListIterator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
public class Activity extends PortContainer {


    @OneToMany(mappedBy = "activity")
    private Set<ResourceUse> resourceUses=new HashSet<ResourceUse>();
    @ManyToOne
    private Capability capabilityRequirement;
    @ManyToOne
    private RoleInCapabilityMethod performingRole;
    @ManyToOne
    private CapabilityMethod collaboration;
    @ManyToOne
    private CapabilityMethod delegatingCollaboration;
    @ManyToOne
    private Measure duration;
    @ManyToMany
    private Set<Measure> measures=new HashSet<Measure>();
    @OneToMany(mappedBy = "delegatingActivity", cascade = CascadeType.ALL)
    private Set<RoleMapping> roleMappings=new HashSet<RoleMapping>();
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private Set<RoleResource> roleResources=new HashSet<RoleResource>();

    public Activity(String uri, RoleInCapabilityMethod performingRole) {
        super(uri);
        this.performingRole = performingRole;
        this.collaboration= performingRole.getCollaboration();
        this.getPerformingRole().getPerformedActitivities().add(this);
        this.getCollaboration().getActivities().add(this);
    }

    public Activity() {
    }

    public CapabilityMethod getCollaboration() {
        return collaboration;
    }

    public RoleInCapabilityMethod getPerformingRole() {
        return performingRole;
    }
    public Capability getCapabilityRequirement() {
        return capabilityRequirement;
    }

    public void setCapabilityRequirement(Capability capabilityRequirement) {
        this.capabilityRequirement = capabilityRequirement;
    }

    public CapabilityMethod getDelegatingCollaboration() {
        return delegatingCollaboration;
    }

    public void setDelegatingCollaboration(CapabilityMethod delegatingCollaboration) {
        this.delegatingCollaboration = delegatingCollaboration;
    }

    public Measure getDuration() {
        return duration;
    }

    public void setDuration(Measure duration) {
        this.duration = duration;
    }

    public Set<Measure> getMeasures() {
        return measures;
    }

    public Set<ResourceUse> getResourceUses() {
        return resourceUses;
    }

    public Measure findMeasure(String name) {
        return findByName(getMeasures(),name);
    }


    public ResourceUse findResourceUse(String name) {
        return findByName(getResourceUses(),name);
    }

    public Set<RoleMapping> getRoleMappings() {
        return roleMappings;
    }

    public Set<RoleResource> getRoleResources() {
        return roleResources;
    }

}
