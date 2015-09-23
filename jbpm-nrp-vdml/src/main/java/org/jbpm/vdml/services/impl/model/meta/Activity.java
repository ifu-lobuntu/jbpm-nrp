package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Activity extends PortContainer {


    @OneToMany(mappedBy = "activity")
    private Set<ResourceUse> resourceUses=new HashSet<ResourceUse>();
    @ManyToOne
    private Capability capabilityRequirement;
    @ManyToOne
    private Role performingRole;
    @ManyToOne
    private Collaboration collaboration;
    @ManyToOne
    private Collaboration delegatingCollaboration;
    @ManyToMany
    private Set<Measure> measures=new HashSet<Measure>();

    public Activity(String uri, Role performingRole) {
        super(uri);
        this.performingRole = performingRole;
        this.collaboration= performingRole.getCollaboration();
        this.getPerformingRole().getPerformedActitivities().add(this);
        this.getCollaboration().getActivities().add(this);
    }

    public Activity() {
    }

    public Collaboration getCollaboration() {
        return collaboration;
    }

    public Role getPerformingRole() {
        return performingRole;
    }
    public Capability getCapabilityRequirement() {
        return capabilityRequirement;
    }

    public void setCapabilityRequirement(Capability capabilityRequirement) {
        this.capabilityRequirement = capabilityRequirement;
    }

    public Collaboration getDelegatingCollaboration() {
        return delegatingCollaboration;
    }

    public void setDelegatingCollaboration(Collaboration delegatingCollaboration) {
        this.delegatingCollaboration = delegatingCollaboration;
    }

    public Set<Measure> getMeasures() {
        return measures;
    }

    public Set<ResourceUse> getResourceUses() {
        return resourceUses;
    }
}
