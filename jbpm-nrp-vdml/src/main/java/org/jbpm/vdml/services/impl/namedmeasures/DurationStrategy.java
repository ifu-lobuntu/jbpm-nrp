package org.jbpm.vdml.services.impl.namedmeasures;


import org.jbpm.vdml.services.impl.NamedMeasureStrategy;
import org.jbpm.vdml.services.impl.model.runtime.ActivityInstance;
import org.jbpm.vdml.services.impl.model.runtime.CollaborationInstance;
import org.jbpm.vdml.services.impl.model.runtime.ResourceUseInstance;
import org.jbpm.vdml.services.impl.model.runtime.RuntimeEntity;

public class DurationStrategy extends AbstractDurationStrategy implements NamedMeasureStrategy{
    @Override
    public Object applyMeasurement(RuntimeEntity entity) {
        if(entity instanceof ActivityInstance){
            ActivityInstance ao  = (ActivityInstance) entity;
            return calcDurationInMinutes(ao.getActualStartDate(), ao.getActualDateOfCompletion());
        }else if(entity instanceof ResourceUseInstance){
            ResourceUseInstance ruo= (ResourceUseInstance) entity;
            return calcDurationInMinutes(ruo.getActualFromDateTime(), ruo.getActualFromDateTime());
        }else if(entity instanceof CollaborationInstance){

        }
        return null;
    }
}
