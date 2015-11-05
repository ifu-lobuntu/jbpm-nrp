package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("RoleInNetwork")
public class RoleInNetwork extends Role{
    @OneToMany(mappedBy = "fulfillingNetworkRole")
    private Set<RoleInCapabilityMethod> fulfilledCapabilityMethodRoles=new HashSet<RoleInCapabilityMethod>();

    public RoleInNetwork(String uri, Collaboration collaboration) {
        super(uri, collaboration);
    }

    public RoleInNetwork() {
    }

    public Set<RoleInCapabilityMethod> getFulfilledCapabilityMethodRoles() {
        return fulfilledCapabilityMethodRoles;
    }
}
