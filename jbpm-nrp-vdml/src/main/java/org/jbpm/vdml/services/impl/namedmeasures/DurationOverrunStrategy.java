package org.jbpm.vdml.services.impl.namedmeasures;


import org.jbpm.vdml.services.impl.NamedMeasureStrategy;
import org.jbpm.vdml.services.impl.model.runtime.ActivityInstance;
import org.jbpm.vdml.services.impl.model.runtime.CollaborationInstance;
import org.jbpm.vdml.services.impl.model.runtime.ResourceUseInstance;
import org.jbpm.vdml.services.impl.model.runtime.RuntimeEntity;

public class DurationOverrunStrategy extends AbstractDurationStrategy implements NamedMeasureStrategy{
    @Override
    public Object applyMeasurement(RuntimeEntity entity) {
        if(entity instanceof ActivityInstance){
            ActivityInstance ao  = (ActivityInstance) entity;
            Double planned = calcDurationInMinutes(ao.getPlannedStartDate(), ao.getPlannedDateOfCompletion());
            Double actual = calcDurationInMinutes(ao.getActualStartDate(), ao.getActualDateOfCompletion());
            return calculateOverrun(planned, actual);
        }else if(entity instanceof ResourceUseInstance){
            ResourceUseInstance ruo= (ResourceUseInstance) entity;
            Double planned = calcDurationInMinutes(ruo.getPlannedFromDateTime(), ruo.getPlannedFromDateTime());
            Double actual = calcDurationInMinutes(ruo.getActualFromDateTime(), ruo.getActualFromDateTime());
            return calculateOverrun(planned, actual);
        }else if(entity instanceof CollaborationInstance){

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
