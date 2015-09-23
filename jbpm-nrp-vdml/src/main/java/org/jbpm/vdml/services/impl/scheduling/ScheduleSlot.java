package org.jbpm.vdml.services.impl.scheduling;


import com.vividsolutions.jts.geom.Point;
import org.joda.time.DateTime;

import java.util.Date;

public class ScheduleSlot {
    Object scheduledObject;
    DateTime from;
    DateTime to;
    ScheduleSlot next;
    Point lastLocation;

    public ScheduleSlot(Object scheduledObject, DateTime from, DateTime to, Point lastLocation) {
        this.scheduledObject = scheduledObject;
        this.from = from;
        this.to = to;
    }

    public Point getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Point lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Object getScheduledObject() {
        return scheduledObject;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public long getDurationInMillis() {
        return getTo().getMillis()- getFrom().getMillis();
    }

    public ScheduleSlot getNext() {
        return next;
    }

    public void setNext(ScheduleSlot next) {
        this.next = next;
    }

    public ScheduleSlot getEndSlot(long remainingDuration) {
        long myDuration = getDurationInMillis();
        if(myDuration >=remainingDuration) {
            return this;
        }else if(getNext()==null){
            return null;//out of range
        }else{
           return getNext().getEndSlot(remainingDuration- myDuration);
        }
    }
}
