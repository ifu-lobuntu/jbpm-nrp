package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class StoreMeasurement extends Measurement{
    @ManyToOne
    private StorePerformance store;

    public StoreMeasurement() {
    }

    public StoreMeasurement(Measure measure, StorePerformance store) {
        super(measure);
        this.store = store;
        this.store.getMeasurements().add(this);
    }

    public StorePerformance getStore() {
        return store;
    }
}
