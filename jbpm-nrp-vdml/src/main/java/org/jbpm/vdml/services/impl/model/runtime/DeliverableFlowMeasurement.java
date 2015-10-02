package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class DeliverableFlowMeasurement extends Measurement{
    @ManyToOne
    private DeliverableFlowInstance deliverableFlowInstance;

    public DeliverableFlowMeasurement() {
    }

    public DeliverableFlowMeasurement(Measure measure, DeliverableFlowInstance deliverableFlowInstance) {
        super(measure);
        this.deliverableFlowInstance = deliverableFlowInstance;
        this.deliverableFlowInstance.getMeasurements().add(this);
    }

    public DeliverableFlowInstance getDeliverableFlowInstance() {
        return deliverableFlowInstance;
    }

    @Override
    public RuntimeEntity getMeasurand() {
        return getDeliverableFlowInstance();
    }
}
