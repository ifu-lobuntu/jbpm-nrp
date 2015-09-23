package org.jbpm.vdml.services.api.model;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class ReusableBusinessItemRequirement implements Serializable{
    private String businessItemDefinitionId;
    private DateTime notBefore;
    private DateTime notAfter;
    private Double quantity;
    private Double longitude;
    private Double lattitude;
    private Double maxDistanceInMeter;
    private Double duration;
    private TimeUnit durationTimeUnit;
    public ReusableBusinessItemRequirement() {
    }

    public TimeUnit getDurationTimeUnit() {
        return durationTimeUnit;
    }

    public void setDurationTimeUnit(TimeUnit durationTimeUnit) {
        this.durationTimeUnit = durationTimeUnit;
    }

    public DateTime getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(DateTime notBefore) {
        this.notBefore = notBefore;
    }

    public DateTime getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(DateTime notAfter) {
        this.notAfter = notAfter;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLattitude() {
        return lattitude;
    }

    public void setLattitude(Double lattitude) {
        this.lattitude = lattitude;
    }

    public String getBusinessItemDefinitionId() {
        return businessItemDefinitionId;
    }

    public void setBusinessItemDefinitionId(String businessItemDefinitionId) {
        this.businessItemDefinitionId = businessItemDefinitionId;
    }

    public Double getMaxDistanceInMeter() {
        return maxDistanceInMeter;
    }

    public void setMaxDistanceInMeter(Double maxDistanceInMeter) {
        this.maxDistanceInMeter = maxDistanceInMeter;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }
}
