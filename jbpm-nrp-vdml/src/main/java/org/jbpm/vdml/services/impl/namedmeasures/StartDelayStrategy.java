package org.jbpm.vdml.services.impl.namedmeasures;


import org.jbpm.vdml.services.impl.NamedMeasureStrategy;
import org.jbpm.vdml.services.impl.model.runtime.ActivityInstance;
import org.jbpm.vdml.services.impl.model.runtime.CollaborationInstance;
import org.jbpm.vdml.services.impl.model.runtime.ResourceUseInstance;
import org.jbpm.vdml.services.impl.model.runtime.RuntimeEntity;

public class StartDelayStrategy extends AbstractDurationStrategy implements NamedMeasureStrategy {
    @Override
    public Object applyMeasurement(RuntimeEntity entity) {
        if(entity instanceof ActivityInstance){
            ActivityInstance ao  = (ActivityInstance) entity;
            return calcDurationInMinutes(ao.getPlannedStartDate(), ao.getActualStartDate());
        }else if(entity instanceof ResourceUseInstance){
            ResourceUseInstance ruo= (ResourceUseInstance) entity;
            return calcDurationInMinutes(ruo.getPlannedFromDateTime(), ruo.getActualFromDateTime());
        }else if(entity instanceof CollaborationInstance){

        }
        return null;
    }
}
