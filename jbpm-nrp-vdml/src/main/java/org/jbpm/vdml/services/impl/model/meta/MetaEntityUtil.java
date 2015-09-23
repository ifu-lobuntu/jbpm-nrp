package org.jbpm.vdml.services.impl.model.meta;


import java.util.Collection;

public class MetaEntityUtil {
    public static <T extends MetaEntity> T findByName(Collection<? extends T> from, String name){
        for (T t : from) {
            if(t.getName().equals(name))
            {
                return t;
            }
        }
        return null;
    }
}
