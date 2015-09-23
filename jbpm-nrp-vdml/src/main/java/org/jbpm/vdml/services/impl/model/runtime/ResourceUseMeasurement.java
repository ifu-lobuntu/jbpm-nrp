package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ResourceUseMeasurement extends Measurement{
    @ManyToOne
    private ResourceUseObservation resourceUse;

    public ResourceUseMeasurement() {
    }

    public ResourceUseMeasurement(Measure measure, ResourceUseObservation resourceUse) {
        super(measure);
        this.resourceUse = resourceUse;
        this.resourceUse.getMeasurements().add(this);
    }

    public ResourceUseObservation getResourceUse() {
        return resourceUse;
    }
}
