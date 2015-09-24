package org.jbpm.vdml.services.impl.namedmeasures;


import org.jbpm.vdml.services.impl.NamedMeasureStrategy;
import org.jbpm.vdml.services.impl.model.runtime.ActivityObservation;
import org.jbpm.vdml.services.impl.model.runtime.CollaborationObservation;
import org.jbpm.vdml.services.impl.model.runtime.ResourceUseObservation;
import org.jbpm.vdml.services.impl.model.runtime.RuntimeEntity;
import org.joda.time.Duration;

public class DurationOverrunStrategy extends AbstractDurationStrategy implements NamedMeasureStrategy{
    @Override
    public Object applyMeasurement(RuntimeEntity entity) {
        if(entity instanceof ActivityObservation){
            ActivityObservation ao  = (ActivityObservation) entity;
            Double planned = calcDurationInMinutes(ao.getPlannedStartDate(), ao.getPlannedDateOfCompletion());
            Double actual = calcDurationInMinutes(ao.getActualStartDate(), ao.getActualDateOfCompletion());
            return calculateOverrun(planned, actual);
        }else if(entity instanceof ResourceUseObservation){
            ResourceUseObservation ruo= (ResourceUseObservation) entity;
            Double planned = calcDurationInMinutes(ruo.getPlannedFromDateTime(), ruo.getPlannedFromDateTime());
            Double actual = calcDurationInMinutes(ruo.getActualFromDateTime(), ruo.getActualFromDateTime());
            return calculateOverrun(planned, actual);
        }else if(entity instanceof CollaborationObservation){

        }
        return null;
    }

    private Double calculateOverrun(Double planned, Double actual) {
        if(actual!=null && planned!=null) {
            return actual - planned;
        }else{
            return null;
        }
    }
}
