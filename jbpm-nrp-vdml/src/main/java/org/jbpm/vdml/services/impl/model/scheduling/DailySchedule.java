package org.jbpm.vdml.services.impl.model.scheduling;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class DailySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Schedule schedule;


    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    @OneToMany(mappedBy = "dailySchedule" ,cascade = CascadeType.ALL)
    private List<PeriodInDay> activeHours=new ArrayList<PeriodInDay>();

    public DailySchedule(Schedule schedule,DayOfWeek dayOfWeek) {
        this(dayOfWeek);
        this.schedule = schedule;
        this.schedule.getHoursOfAvailability().add(this);
    }

    public DailySchedule() {
    }

    public DailySchedule(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public List<PeriodInDay> getActiveHours() {
        return activeHours;
    }
}
