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
    private PortContainer portContainer;
    @ManyToOne
    private InputPort fromResource;
    @ManyToOne
    private RoleInCapabilityMethod toRole;

    public RoleResource(String uri, PortContainer portContainer) {
        this.uri = uri;
        this.portContainer = portContainer;
        this.portContainer.getRoleResources().add(this);
    }

    public PortContainer getPortContainer() {
        return portContainer;
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
