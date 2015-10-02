package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ResourceUseMeasurement extends Measurement{
    @ManyToOne
    private ResourceUseInstance resourceUse;

    public ResourceUseMeasurement() {
    }

    public ResourceUseMeasurement(Measure measure, ResourceUseInstance resourceUse) {
        super(measure);
        this.resourceUse = resourceUse;
        this.resourceUse.getMeasurements().add(this);
    }

    public ResourceUseInstance getResourceUse() {
        return resourceUse;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getResourceUse();
    }
}
