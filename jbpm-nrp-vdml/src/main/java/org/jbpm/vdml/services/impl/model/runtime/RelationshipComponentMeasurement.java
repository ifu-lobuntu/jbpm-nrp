package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class RelationshipComponentMeasurement extends Measurement{
    @ManyToOne
    private RelationshipComponentPerformance component;

    public RelationshipComponentMeasurement() {
    }

    public RelationshipComponentMeasurement(Measure measure, RelationshipComponentPerformance component) {
        super(measure);
        this.component = component;
        this.component.getMeasurements().add(this);
    }

    public RelationshipComponentPerformance getComponent() {
        return component;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getComponent();
    }
}
