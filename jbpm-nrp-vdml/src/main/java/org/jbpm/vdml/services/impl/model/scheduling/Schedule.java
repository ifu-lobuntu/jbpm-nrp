package org.jbpm.vdml.services.impl.model.scheduling;


import org.jbpm.vdml.services.impl.model.runtime.ResourceUseObservation;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Entity
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Double scheduleSlotSize;
    @Enumerated(EnumType.STRING)
    private TimeUnit scheduleSlotTimeUnit;
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<DailySchedule> hoursOfAvailability =new ArrayList<DailySchedule>();
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    //TODO This relationship can grow, ensure past unavailability gets deleted. This is just for planning purposes. For performance purposes we will look at the associated ResourceUse
    private Set<PlannedUnavailability> plannedUnavailability=new HashSet<PlannedUnavailability>() ;

    public List<DailySchedule> getHoursOfAvailability() {
        return hoursOfAvailability;
    }

    public Long getId() {
        return id;
    }

    public Double getScheduleSlotSize() {
        return scheduleSlotSize;
    }

    public TimeUnit getScheduleSlotTimeUnit() {
        return scheduleSlotTimeUnit;
    }

    public Set<PlannedUnavailability> getPlannedUnavailability() {
        return plannedUnavailability;
    }
    public List<PlannedUnavailability> getSortedPlannedUnavailability() {
        List<PlannedUnavailability> result = new ArrayList<PlannedUnavailability>(getPlannedUnavailability());
        Collections.sort(result, new Comparator<PlannedUnavailability>() {
            @Override
            public int compare(PlannedUnavailability o1, PlannedUnavailability o2) {
                return o1.getFrom().compareTo(o2.getFrom());
            }
        });
        return result;
    }

    public void setScheduleSlotSize(Double scheduleSlotSize) {
        this.scheduleSlotSize = scheduleSlotSize;
    }

    public void setScheduleSlotTimeUnit(TimeUnit scheduleSlotTimeUnit) {
        this.scheduleSlotTimeUnit = scheduleSlotTimeUnit;
    }
}
