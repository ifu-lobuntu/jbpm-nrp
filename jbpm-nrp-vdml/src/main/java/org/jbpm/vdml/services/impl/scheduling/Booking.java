package org.jbpm.vdml.services.impl.scheduling;

import com.vividsolutions.jts.geom.Point;
import org.joda.time.DateTime;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.concurrent.TimeUnit;

@PlanningEntity(difficultyWeightFactoryClass = BookingDifficultyWeightFactory.class)
public class Booking {
    private ScheduleSlot startScheduleSlot;
    private DateTime notBefore;
    private DateTime notAfter;
    private int durationInMillis;
    private int score;
    private Point location;

    public Booking(int score) {
        this.score = score;
    }

    public Booking() {

    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Booking(DateTime notBefore, DateTime notAfter, int durationInMillis, Point location) {
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.durationInMillis = durationInMillis;
        this.location = location;
    }


    public Point getLocation() {
        return location;
    }

    public ScheduleSlot getEndScheduleSlot() {
        return startScheduleSlot.getEndSlot(durationInMillis);

    }

    @PlanningVariable(valueRangeProviderRefs = {"scheduleSlots"})
    public ScheduleSlot getStartScheduleSlot() {
        return startScheduleSlot;
    }

    public void setStartScheduleSlot(ScheduleSlot bookableObject) {
        this.startScheduleSlot = bookableObject;
    }

    public DateTime getNotBefore() {
        return notBefore;
    }

    public DateTime getNotAfter() {
        return notAfter;
    }

    public int getDurationInMillis() {
        return durationInMillis;
    }


}
