package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ProvidedValuePropositionComponentMeasurement extends Measurement{
    @ManyToOne
    private ProvidedValuePropositionComponentPerformance component;

    public ProvidedValuePropositionComponentMeasurement(Measure measure, ProvidedValuePropositionComponentPerformance component) {
        super(measure);
        this.component = component;
        this.component.getMeasurements().add(this);
    }

    public ProvidedValuePropositionComponentMeasurement() {
    }

    public ProvidedValuePropositionComponentPerformance getComponent() {
        return component;
    }
}
