package org.jbpm.vdml.services.impl.namedmeasures;


import org.jbpm.vdml.services.impl.NamedMeasureStrategy;
import org.jbpm.vdml.services.impl.model.runtime.ActivityObservation;
import org.jbpm.vdml.services.impl.model.runtime.CollaborationObservation;
import org.jbpm.vdml.services.impl.model.runtime.ResourceUseObservation;
import org.jbpm.vdml.services.impl.model.runtime.RuntimeEntity;

public class CompletionLatenessStrategy extends AbstractDurationStrategy implements NamedMeasureStrategy {
    @Override
    public Object applyMeasurement(RuntimeEntity entity) {
        if(entity instanceof ActivityObservation){
            ActivityObservation ao  = (ActivityObservation) entity;
            return calcDurationInMinutes(ao.getPlannedDateOfCompletion(), ao.getActualDateOfCompletion());
        }else if(entity instanceof ResourceUseObservation){
            ResourceUseObservation ruo= (ResourceUseObservation) entity;
            return calcDurationInMinutes(ruo.getPlannedToDateTime(), ruo.getActualToDateTime());
        }else if(entity instanceof CollaborationObservation){
        }
        return null;
    }
}
