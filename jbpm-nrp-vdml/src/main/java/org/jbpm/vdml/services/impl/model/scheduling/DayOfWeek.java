package org.jbpm.vdml.services.impl.model.scheduling;

import java.util.Calendar;

public enum DayOfWeek {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");//TODO Public Holidays

    public static DayOfWeek valueOfCalendar(int calendarDayInWeek) {
        switch (calendarDayInWeek) {
            case Calendar.SUNDAY:
                return SUNDAY;
            case Calendar.MONDAY:
                return MONDAY;
            case Calendar.TUESDAY:
                return TUESDAY;
            case Calendar.WEDNESDAY:
                return WEDNESDAY;
            case Calendar.THURSDAY:
                return THURSDAY;
            case Calendar.FRIDAY:
                return FRIDAY;
            case Calendar.SATURDAY:
                return SATURDAY;
            default:
                throw new IllegalArgumentException("The calendarDayInWeek (" + calendarDayInWeek
                        + ") is not supported.");
        }
    }

    public static DayOfWeek valueOfCode(String code) {
        for (DayOfWeek dayOfWeek : values()) {
            if (code.equalsIgnoreCase(dayOfWeek.getCode())) {
                return dayOfWeek;
            }
        }
        return null;
    }

    private String code;

    private DayOfWeek(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getDistanceToNext(DayOfWeek other) {
        int distance = other.ordinal() - ordinal();
        if (distance < 0) {
            distance += 7;
        }
        return distance;
    }

    public DayOfWeek determineNextDayOfWeek() {
        switch (this) {
            case MONDAY:
                return TUESDAY;
            case TUESDAY:
                return WEDNESDAY;
            case WEDNESDAY:
                return THURSDAY;
            case THURSDAY:
                return FRIDAY;
            case FRIDAY:
                return SATURDAY;
            case SATURDAY:
                return SUNDAY;
            case SUNDAY:
                return MONDAY;
            default:
                throw new IllegalArgumentException("The dayOfWeek (" + this + ") is not supported.");
        }
    }

    public String getLabel() {
        return code.substring(0, 2);
    }

    public String toString() {
        return code.substring(0, 3);
    }

}
