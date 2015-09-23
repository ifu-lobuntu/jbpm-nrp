package org.jbpm.vdml.services.impl.scheduling;

import org.jbpm.vdml.services.impl.LocationUtil;
import org.joda.time.Duration;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

public class BookingSolutionCalculator implements EasyScoreCalculator<BookingSolution> {
    private final double millisPerMinute = 1000 * 60d;

    @Override
    public SimpleScore calculateScore(BookingSolution bookingSolution) {
        double totalScore = 1;
        for (Booking booking : bookingSolution.getBookings()) {
            if (booking.getStartScheduleSlot() == null ||
                    booking.getEndScheduleSlot() == null) {
                return SimpleScore.valueOf(-1);

            }
            ScheduleSlot currentSlot = booking.getStartScheduleSlot();
            long remainder = booking.getDurationInMillis()-currentSlot.getDurationInMillis();
            long totalIdleMinutes = 0;
            long minutesFromPreferredStart = Math.round(new Duration(booking.getNotBefore(), currentSlot.getFrom()).getMillis() / millisPerMinute);
            while (currentSlot.getNext() != null && remainder > 0) {
                Duration interval = new Duration(currentSlot.getTo(), currentSlot.getNext().getFrom());
                totalIdleMinutes += Math.round(interval.getMillis() / millisPerMinute);
                if (totalIdleMinutes > 120) {
                    return SimpleScore.valueOf(-1);
                }
                currentSlot = currentSlot.getNext();
                remainder -= currentSlot.getDurationInMillis();
            }
            if (currentSlot.getLastLocation() != null && booking.getLocation() != null) {
                double distance = Math.max(1, LocationUtil.degreesToEstimatedMeters(currentSlot.getLastLocation().distance(booking.getLocation())));//can't be zero
                totalScore += Math.max(1, totalIdleMinutes) * Math.max(1, minutesFromPreferredStart) * Math.max(1, distance / 100); //distance in hundreds of meters this is bit relative.
            } else {
                totalScore += Math.max(1, totalIdleMinutes) * Math.max(1, minutesFromPreferredStart);
            }
            System.out.println("Minutes From Start:" + minutesFromPreferredStart);
            System.out.println("Idle Minutes:" + totalIdleMinutes);
        }
        int finalScore = (int) Math.round(1000000000 / totalScore);
        System.out.println("Final Score:" + finalScore);
        return SimpleScore.valueOf(finalScore);
    }
}
