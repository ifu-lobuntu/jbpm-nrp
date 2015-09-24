package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.namedmeasures.CompletionLatenessStrategy;
import org.jbpm.vdml.services.impl.namedmeasures.DurationOverrunStrategy;
import org.jbpm.vdml.services.impl.namedmeasures.DurationStrategy;
import org.jbpm.vdml.services.impl.namedmeasures.StartDelayStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NamedMeasureRegistry {
    private static Map<String,Map<String,NamedMeasureStrategy>> map=new ConcurrentHashMap<String, Map<String,NamedMeasureStrategy>>();
    public static NamedMeasureStrategy get(String deploymentId, String measureName){
        return getForDeployment(deploymentId).get(measureName);
    }
    public static void register(String deploymentId, String measureName, NamedMeasureStrategy s){
        getForDeployment(deploymentId).put(measureName, s);
    }

    private static Map<String, NamedMeasureStrategy> getForDeployment(String deploymentId) {
        Map<String, NamedMeasureStrategy> result = map.get(deploymentId);
        if(result==null){
            map.put(deploymentId,result=new HashMap<String, NamedMeasureStrategy>());
            result.put("Duration", new DurationStrategy());
            result.put("StartDelay", new StartDelayStrategy());
            result.put("DurationOverrun", new DurationOverrunStrategy());
            result.put("CompletionLateness", new CompletionLatenessStrategy());
        }
        return result;
    }
}
