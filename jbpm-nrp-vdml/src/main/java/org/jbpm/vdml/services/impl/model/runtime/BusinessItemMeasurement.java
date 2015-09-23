package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class BusinessItemMeasurement extends Measurement{
    @ManyToOne
    private BusinessItemObservation businessItem;

    public BusinessItemMeasurement() {
    }

    public BusinessItemMeasurement(Measure measure, BusinessItemObservation businessItem){
        super(measure);
        this.businessItem=businessItem;
        businessItem.getMeasurements().add(this);
    }

    public BusinessItemObservation getBusinessItem(){
        return this.businessItem;
    }
}
