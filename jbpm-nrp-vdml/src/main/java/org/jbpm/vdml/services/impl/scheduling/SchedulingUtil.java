package org.jbpm.vdml.services.impl.scheduling;

import org.jbpm.vdml.services.impl.model.scheduling.*;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.base.BaseDateTime;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SchedulingUtil {

    public static List<ScheduleSlot> calculate(SchedulableObject scheduledObject, Schedule workingHours, BaseDateTime from, BaseDateTime to) {
        List<PlannedUnavailability> plannedUnavailability = workingHours.getSortedPlannedUnavailability();
        List<ScheduleSlot> result = new ArrayList<ScheduleSlot>();
        MutableDateTime curCal = new MutableDateTime(from);
        int slotInMillis = durationToMillis(workingHours.getScheduleSlotSize(), workingHours.getScheduleSlotTimeUnit());
        ScheduleSlot previous = null;
        while (curCal.getMillis() <= to.getMillis()) {
            DailySchedule dailySchedule = getScheduleFor(workingHours, curCal);
            for (PeriodInDay p : dailySchedule.getActiveHours()) {
                curCal.setMillisOfDay(p.startMillis());
                while (curCal.getMillisOfDay() < p.endMillis()) {
                    int endMillis = Math.min(curCal.getMillisOfDay() + slotInMillis, p.endMillis());
                    if (curCal.getMillis() >= from.getMillis() && curCal.getMillis() <= to.getMillis()) {
                        PlannedUnavailability closestPrecedingUnavailability = getClosestPrecedingUnavailability(curCal, plannedUnavailability);
                        if (closestPrecedingUnavailability==null || closestPrecedingUnavailability.getTo().isBefore(curCal)) {
                            DateTime periodFrom = new DateTime(curCal);
                            ScheduleSlot newSlot = new ScheduleSlot(scheduledObject, periodFrom, periodFrom.withMillisOfDay(endMillis), closestPrecedingUnavailability==null?null:closestPrecedingUnavailability.getLocation());
                            if(closestPrecedingUnavailability!=null && closestPrecedingUnavailability.getLocation()!=null){
                                newSlot.setLastLocation(closestPrecedingUnavailability.getLocation());
                            }else{
                                newSlot.setLastLocation(scheduledObject.getLocation());
                            }
                            if (previous != null) {
                                previous.setNext(newSlot);
                            }
                            result.add(newSlot);
//                            System.out.println(newSlot.getFrom() +" to " + newSlot.getTo());
                            previous = newSlot;
                        }
                    }
                    curCal.setMillisOfDay(endMillis);
                }
            }
            curCal.addDays(1);
        }
        return result;
    }

    private static  PlannedUnavailability getClosestPrecedingUnavailability(BaseDateTime curCal, List<PlannedUnavailability> occupiedPeriods) {
        PlannedUnavailability previous = null;
        for (PlannedUnavailability period : occupiedPeriods) {
            if (period.getFrom().isBefore(curCal) && period.getTo().isAfter(curCal)) {
                return period;
            } else if (period.getFrom().isAfter(curCal)) {
                return previous;
            }
            previous = period;
        }
        return null;
    }

    public static int durationToMillis(double slotSize, TimeUnit slotSizeTimeUnit) {
        switch (slotSizeTimeUnit) {
            case MILLISECONDS:
                return (int) Math.round(slotSize);
            case SECONDS:
                return durationToMillis(slotSize * 1000, TimeUnit.MILLISECONDS);
            case MINUTES:
                return durationToMillis(slotSize * 60, TimeUnit.SECONDS);
            case HOURS:
                return durationToMillis(slotSize * 60, TimeUnit.MINUTES);
            case DAYS:
                return durationToMillis(slotSize * 24, TimeUnit.HOURS);
        }
        return 0;
    }

    private static DailySchedule getScheduleFor(Schedule workingHours, BaseDateTime date) {
        DayOfWeek dayOfWeek = DayOfWeek.valueOfCalendar(date.getDayOfWeek());
        for (DailySchedule dailySchedule : workingHours.getHoursOfAvailability()) {
            if (dailySchedule.getDayOfWeek() == dayOfWeek) {
                return dailySchedule;
            }
        }
        throw new IllegalStateException();
    }
}
