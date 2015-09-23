package org.jbpm.vdml.services.impl.model.scheduling;

import javax.persistence.*;

@Entity
public class PeriodInDay  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private DailySchedule dailySchedule;
    private int fromMinutesOfDay;
    private int toMinutesOfDay;

    public PeriodInDay() {
    }

    public PeriodInDay( DailySchedule dailySchedule, int fromHours, int fromMinutes, int toHours, int toMinutes) {
        this(dailySchedule,fromHours*60+fromMinutes,toHours*60+toMinutes);
    }

    public PeriodInDay(DailySchedule dailySchedule, int fromMinutesOfDay, int toMinutesOfDay) {
        this(dailySchedule);
        this.fromMinutesOfDay = fromMinutesOfDay;
        this.toMinutesOfDay = toMinutesOfDay;
    }

    public PeriodInDay(DailySchedule dailySchedule) {
        this.dailySchedule=dailySchedule;
        this.dailySchedule.getActiveHours().add(this);
    }


    public int startMillis(){
        return toMillis(fromMinutesOfDay);
    }
    public int endMillis(){
        return toMillis(toMinutesOfDay);
    }

    private int toMillis(int min) {
        return min*60*1000;
    }

    public int getFromMinutesOfDay() {
        return fromMinutesOfDay;
    }

    public void setFromMinutesOfDay(int fromMinutesOfDay) {
        this.fromMinutesOfDay = fromMinutesOfDay;
    }

    public int getToMinutesOfDay() {
        return toMinutesOfDay;
    }

    public void setToMinutesOfDay(int toMinutesOfDay) {
        this.toMinutesOfDay = toMinutesOfDay;
    }
}
