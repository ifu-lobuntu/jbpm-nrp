package org.jbpm.vdml.services.impl.scheduling;


import org.joda.time.DateTime;

import java.util.List;

public class BookableObject {
    private List<ScheduleSlot> slots;
    private String name;
    private int score;

    public BookableObject(int score) {
        this.score = score;
    }

    public BookableObject(String name, List<ScheduleSlot> slots) {
        this.slots = slots;
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<ScheduleSlot> getSlots() {
        return slots;
    }

    public boolean supports(DateTime notBefore, DateTime notAfter, long durationInMillis) {
        long duration=0;
        for (ScheduleSlot slot : slots) {
            duration+=slot.getDurationInMillis();
            if(duration>=durationInMillis){
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }
}
