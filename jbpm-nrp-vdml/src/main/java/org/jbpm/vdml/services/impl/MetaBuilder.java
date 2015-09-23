package org.jbpm.vdml.services.impl;


import org.eclipse.emf.ecore.EObject;
import org.jbpm.designer.extensions.emf.util.JBPMECoreHelper;
import org.jbpm.vdml.services.impl.model.meta.*;

import javax.persistence.EntityManager;
import java.lang.reflect.Constructor;

public class MetaBuilder {
    EntityManager entityManager;

    public MetaBuilder() {
    }

    public MetaBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected EmfReference buildReference(EObject a) {
        EmfReference er = new EmfReference(buildUri(a));
        return er;
    }

    public static String buildUri(EObject a) {
        return a.eResource().getURI().toPlatformString(true) + "#" + JBPMECoreHelper.getID(a);
    }


    protected <T> T findOrCreate(EObject eo, Class<T> t, Object... parent) {
        if (eo == null) {
            return null;
        }
        T result = entityManager.find(t, buildUri(eo));
        if (result == null) {
            try {
                Object[] args = new Object[parent.length + 1];
                args[0] = buildUri(eo);
                int i = 1;
                for (Object o : parent) {
                    args[i++] = o;
                }
                Constructor<T> constr = null;
                outer:
                for (Constructor<?> constructor : t.getConstructors()) {
                    Class<?>[] types = constructor.getParameterTypes();
                    if (types.length == args.length) {
                        for (int j = 0; j < types.length; j++) {
                            if (!types[j].isInstance(args[j])) {
                                continue outer;
                            }
                            constr = (Constructor<T>) constructor;
                            break;
                        }
                    }
                }
                result = constr.newInstance(args);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            entityManager.persist(result);
            entityManager.flush();
        }
        return result;
    }
}
