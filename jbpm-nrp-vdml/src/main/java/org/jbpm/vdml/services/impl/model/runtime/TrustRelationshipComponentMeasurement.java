package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class TrustRelationshipComponentMeasurement extends Measurement{
    @ManyToOne
    private TrustRelationshipComponent component;

    public TrustRelationshipComponentMeasurement() {
    }

    public TrustRelationshipComponentMeasurement(Measure measure, TrustRelationshipComponent component) {
        super(measure);
        this.component = component;
        this.component.getMeasurements().add(this);
    }

    public TrustRelationshipComponent getComponent() {
        return component;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getComponent();
    }
}
