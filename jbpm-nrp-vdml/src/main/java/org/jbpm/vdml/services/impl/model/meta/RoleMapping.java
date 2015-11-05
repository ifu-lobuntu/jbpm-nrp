package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class RoleMapping implements MetaEntity{
    @Id
    private String uri;
    private String name;
    @ManyToOne
    private Activity delegatingActivity;
    @ManyToOne
    private RoleInCapabilityMethod fromRole;
    @ManyToOne
    private RoleInCapabilityMethod toRole;

    public RoleMapping(String uri,Activity delegatingActivity ) {
        this.uri = uri;
        this.delegatingActivity = delegatingActivity;
        this.delegatingActivity.getRoleMappings().add(this);
    }

    public Activity getDelegatingActivity() {
        return delegatingActivity;
    }

    public RoleInCapabilityMethod getFromRole() {
        return fromRole;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setFromRole(RoleInCapabilityMethod fromRole) {
        this.fromRole = fromRole;
    }

    public RoleInCapabilityMethod getToRole() {
        return toRole;
    }

    public void setToRole(RoleInCapabilityMethod toRole) {
        this.toRole = toRole;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
