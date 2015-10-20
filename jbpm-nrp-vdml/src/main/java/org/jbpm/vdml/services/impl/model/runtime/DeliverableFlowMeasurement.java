package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class DeliverableFlowMeasurement extends Measurement{
    @ManyToOne
    private DeliverableFlowInstance deliverableFlow;

    public DeliverableFlowMeasurement() {
    }

    public DeliverableFlowMeasurement(Measure measure, DeliverableFlowInstance deliverableFlow) {
        super(measure);
        this.deliverableFlow = deliverableFlow;
        this.deliverableFlow.getMeasurements().add(this);
    }

    public DeliverableFlowInstance getDeliverableFlow() {
        return deliverableFlow;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getDeliverableFlow();
    }
}
