package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Measure;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ReusableBusinessItemMeasurement extends Measurement{
    @ManyToOne
    private ReusableBusinessItemPerformance reusableBusinessItemPerformance;

    public ReusableBusinessItemMeasurement() {
    }

    public ReusableBusinessItemMeasurement(Measure measure, ReusableBusinessItemPerformance reusableBusinessItemPerformance) {
        super(measure);
        this.reusableBusinessItemPerformance = reusableBusinessItemPerformance;
        this.reusableBusinessItemPerformance.getMeasurements().add(this);
    }

    public ReusableBusinessItemPerformance getReusableBusinessItemPerformance() {
        return reusableBusinessItemPerformance;
    }
}
