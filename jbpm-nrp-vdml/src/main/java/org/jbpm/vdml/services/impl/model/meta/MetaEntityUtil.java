package org.jbpm.vdml.services.impl.model.meta;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MetaEntityUtil {
    public static <T extends MetaEntity> T findByName(Collection<? extends T> from, String name) {
        for (T t : from) {
            if (t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public static <T extends MetaEntity> Set<T> findByType(Collection<? super T> from, Class<T> c) {
        Set<T> result = new HashSet<T>();
        for (Object o : from) {
            if (c.isInstance(o)) {

                result.add(c.cast(o));
            }
        }
        return result;
    }

    public static <T extends MetaEntity> T findOneByType(Collection<? super T> from, Class<T> c) {
        Set<T> result = new HashSet<T>();
        for (Object o : from) {
            if (c.isInstance(o)) {
                return c.cast(o);
            }
        }
        return null;
    }
}
