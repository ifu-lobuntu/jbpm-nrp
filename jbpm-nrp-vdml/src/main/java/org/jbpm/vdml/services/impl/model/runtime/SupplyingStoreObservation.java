package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.SupplyingStore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class SupplyingStoreObservation extends PortContainerObservation {
    @ManyToOne
    private SupplyingStoreObservation extendedSupplyingStoreObservation;
    @ManyToOne
    private StorePerformance store;
    @ManyToOne
    private CollaborationObservation collaboration;
    @ManyToOne
    private SupplyingStore supplyingStore;
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private Set<SupplyingStoreMeasurement> measurements = new HashSet<SupplyingStoreMeasurement>();//Aggregated from BusinessItemObservation

    public SupplyingStoreObservation() {
    }

    public SupplyingStoreObservation(SupplyingStore supplyingStore, CollaborationObservation collaboration) {
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

    public CollaborationObservation getCollaboration() {
        return collaboration;
    }

    public SupplyingStore getSupplyingStore() {
        return supplyingStore;
    }

    public Set<SupplyingStoreMeasurement> getMeasurements() {
        return measurements;
    }

    public SupplyingStoreObservation getExtendedSupplyingStoreObservation() {
        return extendedSupplyingStoreObservation;
    }

    public void setExtendedSupplyingStoreObservation(SupplyingStoreObservation extendedSupplyingStoreObservation) {
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

