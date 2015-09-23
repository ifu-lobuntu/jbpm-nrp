package org.jbpm.vdml.services.impl.scheduling;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;

/**
 * Created by ampie on 2015/09/18.
 */
public class BookingDifficultyWeightFactory implements SelectionSorterWeightFactory<BookingSolution,Booking> {
    @Override
    public Comparable createSorterWeight(BookingSolution bookingSolution, Booking booking) {
        return new X(booking);
    }
    public static class X implements Comparable<X>{
        Booking booking;

        public X(Booking booking) {
            this.booking = booking;
        }

        @Override
        public int compareTo(X o) {
            return booking.getScore()-o.booking.getScore();
        }
    }
}
