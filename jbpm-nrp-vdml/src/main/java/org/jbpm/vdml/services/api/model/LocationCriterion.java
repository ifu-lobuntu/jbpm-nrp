package org.jbpm.vdml.services.api.model;


import java.io.Serializable;

public class LocationCriterion implements Serializable {
    private double longitude;
    private double lattitude;
    private double distance;

    public LocationCriterion() {
    }

    public LocationCriterion(double longitude, double lattitude, double distance) {
        this.longitude = longitude;
        this.lattitude = lattitude;
        this.distance = distance;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLattitude() {
        return lattitude;
    }

    public double getDistance() {
        return distance;
    }
}
