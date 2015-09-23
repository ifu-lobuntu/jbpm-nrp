package org.jbpm.vdml.services.impl;

public class LocationUtil {
    public static double meterToEstimatedDegrees(double distanceInMeter) {
        return (360* distanceInMeter)/ (2*Math.PI*6371000);
    }

    public static double degreesToEstimatedMeters(double distanceInDegrees) {
        return (distanceInDegrees / 360) * 2 * Math.PI * 6371000;
    }
}
