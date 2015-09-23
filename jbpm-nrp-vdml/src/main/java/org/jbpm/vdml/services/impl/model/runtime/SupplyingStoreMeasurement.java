package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class SupplyingStoreMeasurement extends Measurement{
    @ManyToOne
    private SupplyingStoreObservation store;

    public SupplyingStoreMeasurement() {
    }

    public SupplyingStoreMeasurement(Measure measure, SupplyingStoreObservation store) {
        super(measure);
        this.store = store;
        this.store.getMeasurements().add(this);
    }

    public SupplyingStoreObservation getStore() {
        return store;
    }
}
