package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
public class SupplyingStore extends PortContainer {

    @ManyToOne
    private StoreDefinition storeRequirement;
    @ManyToOne
    private RoleInCapabilityMethod supplyingRole;
    @ManyToOne
    private CapabilityMethod collaboration;
    @ManyToMany
    private Set<Measure> measures = new HashSet<Measure>();//Aggregated from resource.measures

    public SupplyingStore(String uri, RoleInCapabilityMethod supplyingRole) {
        super(uri);
        this.supplyingRole = supplyingRole;
        this.supplyingRole.getSupplyingStores().add(this);
        this.collaboration=this.getSupplyingRole().getCollaboration();
        this.getCollaboration().getSupplyingStores().add(this);
    }

    public SupplyingStore() {
    }

    public RoleInCapabilityMethod getSupplyingRole() {
        return supplyingRole;
    }

    public CapabilityMethod getCollaboration() {
        return collaboration;
    }

    public StoreDefinition getStoreRequirement() {
        return storeRequirement;
    }

    public void setStoreRequirement(StoreDefinition resource) {
        this.storeRequirement = resource;
    }

    public Set<Measure> getMeasures() {
        return measures;
    }

    public Measure findMeasure(String name) {
        return findByName(getMeasures(),name);
    }
}
