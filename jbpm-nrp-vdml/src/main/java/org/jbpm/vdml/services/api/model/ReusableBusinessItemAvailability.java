package org.jbpm.vdml.services.api.model;


import org.joda.time.DateTime;

import java.io.Serializable;

public class ReusableBusinessItemAvailability implements Serializable{
    private Long reusableBusinessItemId;
    private DateTime from;
    private DateTime to;

    public ReusableBusinessItemAvailability() {
    }

    public ReusableBusinessItemAvailability(Long reusableBusinessItemId, DateTime from, DateTime to) {
        this.reusableBusinessItemId = reusableBusinessItemId;
        this.from = from;
        this.to = to;
    }

    public Long getReusableBusinessItemId() {
        return reusableBusinessItemId;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }
}
