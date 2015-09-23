package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ValueAddMeasurement extends Measurement{
    @ManyToOne
    private DirectedFlowObservation deliverableFlow;

    public ValueAddMeasurement() {
    }

    public ValueAddMeasurement(Measure measure, DirectedFlowObservation deliverableFlow) {
        super(measure);
        this.deliverableFlow = deliverableFlow;
        this.deliverableFlow.getValueAddMeasurements().add(this);
    }

    public DirectedFlowObservation getDeliverableFlow() {
        return deliverableFlow;
    }
}
