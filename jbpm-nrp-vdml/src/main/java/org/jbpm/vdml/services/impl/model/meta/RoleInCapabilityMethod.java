package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("RoleInCapabilityMethod")
public class RoleInCapabilityMethod extends  Role{

    @ManyToOne
    private RoleInNetwork fulfillingNetworkRole;

    public RoleInCapabilityMethod(String uri, Collaboration collaboration) {
        super(uri, collaboration);
    }

    public RoleInCapabilityMethod() {
    }

    public RoleInNetwork getFulfillingNetworkRole() {
        return fulfillingNetworkRole;
    }

    public void setFulfillingNetworkRole(RoleInNetwork fulfillingNetworkRole) {
        this.fulfillingNetworkRole = fulfillingNetworkRole;
    }
    public CapabilityMethod getCollaboration(){
        return (CapabilityMethod) super.getCollaboration();
    }
}
