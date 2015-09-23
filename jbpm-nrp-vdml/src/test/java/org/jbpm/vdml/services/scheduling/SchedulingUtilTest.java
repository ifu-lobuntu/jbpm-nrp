package org.jbpm.vdml.services.scheduling;


import com.vividsolutions.jts.geom.Point;
import org.jbpm.vdml.services.impl.model.scheduling.*;
import org.jbpm.vdml.services.impl.scheduling.*;
import org.joda.time.DateTime;
import org.joda.time.base.BaseDateTime;
import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class SchedulingUtilTest {
    @Test
    public void test(){
        DateTime from = new DateTime(2015, 10, 1, 12, 0, 0, 0);
        DateTime to = new DateTime(2015, 10, 2, 18, 0, 0, 0);

        List<ScheduleSlot> slots = generate16(from,to);
        assertEquals(11,slots.size());
    }

    private static List<ScheduleSlot> generate16(BaseDateTime from, BaseDateTime to) {
        final Schedule schedule = buildSchedule();
        return new SchedulingUtil().calculate(new SchedulableObject() {
            @Override
            public Schedule getSchedule() {
                return schedule;
            }

            @Override
            public Point getLocation() {
                return null;
            }
        }, schedule, from, to);
    }

    public static Schedule buildSchedule() {
        Schedule schedule=new Schedule();
        schedule.getHoursOfAvailability().add(dailySchedule(schedule, DayOfWeek.SUNDAY));
        schedule.getHoursOfAvailability().add(dailySchedule(schedule,DayOfWeek.MONDAY, "7:00-12:00", "13:00-16:00"));
        schedule.getHoursOfAvailability().add(dailySchedule(schedule,DayOfWeek.TUESDAY, "7:00-12:00", "13:00-16:00"));
        schedule.getHoursOfAvailability().add(dailySchedule(schedule,DayOfWeek.WEDNESDAY, "7:00-12:00", "13:00-16:00"));
        schedule.getHoursOfAvailability().add(dailySchedule(schedule,DayOfWeek.THURSDAY, "7:00-12:00", "13:00-16:00"));
        schedule.getHoursOfAvailability().add(dailySchedule(schedule,DayOfWeek.FRIDAY, "7:00-12:00", "13:00-16:00"));
        schedule.getHoursOfAvailability().add(dailySchedule(schedule,DayOfWeek.SATURDAY, "9:00-12:00"));
        schedule.setScheduleSlotSize(1d);
        schedule.setScheduleSlotTimeUnit(TimeUnit.HOURS);
        return schedule;
    }

    @Test
    public void testBooking(){
        DateTime from = new DateTime(2015, 10, 1, 12, 0, 0, 0);
        DateTime to = new DateTime(2015, 10, 2, 18, 0, 0, 0);
        // Build the Solver
        SolverFactory solverFactory = SolverFactory.createFromXmlResource(
                "org/jbpm/vdml/services/impl/scheduling/Booking.xml");
        Solver solver = solverFactory.buildSolver();

        // Load a problem with 400 computers and 1200 processes
        BookingSolution booking = new BookingSolution();
        booking.setScheduleSlots(generate16(from,to));
        Booking booking1 = new Booking(from, to, SchedulingUtil.durationToMillis(3d, TimeUnit.HOURS),null);
        booking.setBookings(Arrays.asList(booking1));


        // Solve the problem
        solver.solve(booking);
        BookingSolution solvedCloudBalance = (BookingSolution) solver.getBestSolution();
        DateTime dateTime = solvedCloudBalance.getBookings().get(0).getStartScheduleSlot().getFrom();
        System.out.println(new BookingSolutionCalculator().calculateScore(solvedCloudBalance));
        assertEquals(1, dateTime.getDayOfMonth());
        assertEquals(13, dateTime.getHourOfDay());

    }
    protected static DailySchedule dailySchedule(Schedule schedule, DayOfWeek sunday, String ... periods) {
        DailySchedule dailySchedule = new DailySchedule(schedule, sunday);
        for (String period : periods) {
            String[] split=period.split("[\\:\\-]");
            new PeriodInDay(dailySchedule, i(split[0]),i(split[1]),i(split[2]),i(split[3]));
        }
        return dailySchedule;
    }

    private static int i(String s) {
        return Integer.parseInt(s);
    }
}
