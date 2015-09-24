package org.jbpm.vdml.services.impl.namedmeasures;


import org.joda.time.DateTime;

public class AbstractDurationStrategy {
    Double calcDurationInMinutes(DateTime from, DateTime to){
        if(from==null||to==null){
            return null;
        }
        long l = to.getMillis() - from.getMillis();
        return toMinutes(l);
    }

    public double toMinutes(long l) {
        return l /(1000*60);
    }
}
