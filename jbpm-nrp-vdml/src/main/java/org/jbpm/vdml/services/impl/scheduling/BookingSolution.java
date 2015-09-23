package org.jbpm.vdml.services.impl.scheduling;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
@PlanningSolution
public class BookingSolution implements Solution<SimpleScore> {
    private List<ScheduleSlot> scheduleSlots;
    private List<Booking> bookings;
    private SimpleScore score;


    @ValueRangeProvider(id = "scheduleSlots")
    public List<ScheduleSlot> getScheduleSlots() {
        return scheduleSlots;
    }

    public void setScheduleSlots(List<ScheduleSlot> scheduleSlots) {
        this.scheduleSlots = scheduleSlots;
    }

    @PlanningEntityCollectionProperty
    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }




    public SimpleScore getScore() {
        return score;
    }


    public void setScore(SimpleScore score) {
        this.score = score;
    }


    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        return facts;
    }
}
