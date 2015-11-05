package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class RoleResource implements MetaEntity{
    @Id
    private String uri;
    private String name;
    @ManyToOne
    private Activity activity;
    @ManyToOne
    private InputPort fromResource;
    @ManyToOne
    private RoleInCapabilityMethod toRole;

    public RoleResource(String uri, Activity activity) {
        this.uri = uri;
        this.activity = activity;
        this.activity.getRoleResources().add(this);
    }

    public Activity getActivity() {
        return activity;
    }

    public InputPort getFromResource() {
        return fromResource;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setFromResource(InputPort fromRole) {
        this.fromResource = fromRole;
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
