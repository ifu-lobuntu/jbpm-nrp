package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class SupplyingStoreMeasurement extends Measurement{
    @ManyToOne
    private SupplyingStoreInstance store;

    public SupplyingStoreMeasurement() {
    }

    public SupplyingStoreMeasurement(Measure measure, SupplyingStoreInstance store) {
        super(measure);
        this.store = store;
        this.store.getMeasurements().add(this);
    }

    public SupplyingStoreInstance getStore() {
        return store;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getStore();
    }
}
