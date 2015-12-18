package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class SupplyingStoreInstance extends PortContainerInstance {
    @ManyToOne
    private SupplyingStoreInstance extendedSupplyingStoreObservation;
    @ManyToOne
    private StorePerformance store;
    @ManyToOne
    private CollaborationInstance collaboration;
    @ManyToOne
    private SupplyingStore supplyingStore;
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private Set<SupplyingStoreMeasurement> measurements = new HashSet<SupplyingStoreMeasurement>();//Aggregated from BusinessItemObservation

    public SupplyingStoreInstance() {
    }

    @Override
    public PortContainer getPortContainer() {
        return supplyingStore;
    }

    @Override
    public RoleInCapabilityMethod getResponsibleRole() {
        return getSupplyingStore().getSupplyingRole();
    }

    public SupplyingStoreInstance(SupplyingStore supplyingStore, CollaborationInstance collaboration) {
        super();
        this.supplyingStore = supplyingStore;
        this.collaboration=collaboration;
        this.getCollaboration().getSupplyingStores().add(this);
    }

    public void setSupplyingRole(RolePerformance supplyingRole) {
        setResponsibleRolePerformance(supplyingRole);
    }

    public void setStore(StorePerformance store) {
        this.store = store;
    }

    public RolePerformance getSupplyingRole() {
        return getResponsibleRolePerformance();
    }

    public CollaborationInstance getCollaboration() {
        return collaboration;
    }

    public SupplyingStore getSupplyingStore() {
        return supplyingStore;
    }

    public Set<SupplyingStoreMeasurement> getMeasurements() {
        return measurements;
    }

    public SupplyingStoreInstance getExtendedSupplyingStoreObservation() {
        return extendedSupplyingStoreObservation;
    }

    public void setExtendedSupplyingStoreObservation(SupplyingStoreInstance extendedSupplyingStoreObservation) {
        this.extendedSupplyingStoreObservation = extendedSupplyingStoreObservation;
    }

    public StorePerformance getStore() {
        return store;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return supplyingStore;
    }

    public SupplyingStoreMeasurement findMeasurement(Measure measure) {
        return findMatchingRuntimeEntity(getMeasurements(),measure);
    }
}

