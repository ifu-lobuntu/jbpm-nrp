package org.jbpm.vdml.services.impl.namedmeasures;


import org.jbpm.vdml.services.impl.NamedMeasureStrategy;
import org.jbpm.vdml.services.impl.model.runtime.*;

public class CompletionLatenessStrategy extends AbstractDurationStrategy implements NamedMeasureStrategy {
    @Override
    public Object applyMeasurement(RuntimeEntity entity) {
        if(entity instanceof ActivityInstance){
            ActivityInstance ao  = (ActivityInstance) entity;
            return calcDurationInMinutes(ao.getPlannedDateOfCompletion(), ao.getActualDateOfCompletion());
        }else if(entity instanceof ResourceUseInstance){
            ResourceUseInstance ruo= (ResourceUseInstance) entity;
            return calcDurationInMinutes(ruo.getPlannedToDateTime(), ruo.getActualToDateTime());
        }else if(entity instanceof DeliverableFlowInstance){
            DeliverableFlowInstance dfo= (DeliverableFlowInstance) entity;
            return calcDurationInMinutes(dfo.getPlannedDate(), dfo.getActualDate());
        }else if(entity instanceof CollaborationInstance){
        }
        return null;
    }
}
