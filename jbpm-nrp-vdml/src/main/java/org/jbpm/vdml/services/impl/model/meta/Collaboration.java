package org.jbpm.vdml.services.impl.model.meta;

import org.jbpm.vdml.services.impl.model.runtime.Address;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
@DiscriminatorColumn(name="type")
public abstract class Collaboration extends PortContainer {
    private String deploymentId;
    @OneToMany(mappedBy = "collaboration",cascade = CascadeType.ALL)
    private Set<Role> collaborationRoles =new HashSet<Role>();
    @ManyToMany(targetEntity = BusinessItemDefinition.class)
    private Set<BusinessItemDefinition> businessItemDefinitions =new HashSet<BusinessItemDefinition>();
    @OneToMany(mappedBy = "owningCollaboration", cascade = CascadeType.ALL)
    private Set<DirectedFlow> ownedDirectedFlows =new HashSet<DirectedFlow>();
    @ManyToOne

    private Address address;

    public Collaboration() {
    }

    public Collaboration(String uri) {
        super(uri);
    }

    public Set<DirectedFlow> getFlows() {
        return ownedDirectedFlows;
    }

    public Set<Role> getCollaborationRoles() {
        return collaborationRoles;
    }

    public Set<BusinessItemDefinition> getBusinessItemDefinitions() {
        return businessItemDefinitions;
    }

    @Override
    public Collection<Measure> getMeasures() {
        return Collections.emptySet();
    }

    public Role findRole(String roleName) {
        return findByName(getCollaborationRoles(), roleName);
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

    public BusinessItemDefinition findBusinessItem(String name) {
        return findByName(getBusinessItemDefinitions(),name);
    }

    public DeliverableFlow findDeliverableFlow(String name) {
        return findByName(getDeliverableFlowsFrom(getOwnedDirectedFlows()),name);
    }

    public Set<DirectedFlow> getOwnedDirectedFlows() {
        return ownedDirectedFlows;
    }


}
