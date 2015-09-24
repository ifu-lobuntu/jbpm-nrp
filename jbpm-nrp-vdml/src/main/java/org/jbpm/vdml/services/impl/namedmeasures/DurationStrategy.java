package org.jbpm.vdml.services.impl.namedmeasures;


import org.jbpm.vdml.services.impl.NamedMeasureStrategy;
import org.jbpm.vdml.services.impl.model.runtime.ActivityObservation;
import org.jbpm.vdml.services.impl.model.runtime.CollaborationObservation;
import org.jbpm.vdml.services.impl.model.runtime.ResourceUseObservation;
import org.jbpm.vdml.services.impl.model.runtime.RuntimeEntity;

public class DurationStrategy extends AbstractDurationStrategy implements NamedMeasureStrategy{
    @Override
    public Object applyMeasurement(RuntimeEntity entity) {
        if(entity instanceof ActivityObservation){
            ActivityObservation ao  = (ActivityObservation) entity;
            return calcDurationInMinutes(ao.getActualStartDate(), ao.getActualDateOfCompletion());
        }else if(entity instanceof ResourceUseObservation){
            ResourceUseObservation ruo= (ResourceUseObservation) entity;
            return calcDurationInMinutes(ruo.getActualFromDateTime(), ruo.getActualFromDateTime());
        }else if(entity instanceof CollaborationObservation){

        }
        return null;
    }
}
