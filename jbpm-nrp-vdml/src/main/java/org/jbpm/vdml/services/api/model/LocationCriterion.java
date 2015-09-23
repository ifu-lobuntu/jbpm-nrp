package org.jbpm.vdml.services.api.model;


import java.io.Serializable;

public class LocationCriterion implements Serializable {
    private String longitude;
    private String lattitude;
    private double distance;

    public LocationCriterion() {
    }

    public LocationCriterion(String longitude, String lattitude, double distance) {
        this.longitude = longitude;
        this.lattitude = lattitude;
        this.distance = distance;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLattitude() {
        return lattitude;
    }

    public double getDistance() {
        return distance;
    }
}
