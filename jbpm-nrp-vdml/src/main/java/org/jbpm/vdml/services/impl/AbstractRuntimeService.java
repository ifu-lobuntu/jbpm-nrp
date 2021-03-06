package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.Role;
import org.jbpm.vdml.services.impl.model.meta.RoleInNetwork;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ampie on 2015/09/14.
 */
public class AbstractRuntimeService extends MetaBuilder {
    EntityManager entityManager;

    public AbstractRuntimeService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AbstractRuntimeService() {
    }

    protected <T extends RuntimeEntity> Collection<T> syncRuntimeEntities(Collection<? extends T> existingRuntimeEntities, Collection<? extends MetaEntity> equivalentMetaEntities, Class<? extends T> cls, Object parent, MetaEntity ... additionalEntities) {
        Set<MetaEntity> source= new HashSet<MetaEntity>(equivalentMetaEntities);
        Set<T> result = new HashSet<T>();
        for (MetaEntity additionalEntity : additionalEntities) {
            if(additionalEntity!=null){
                source.add(additionalEntity);
            }
        }
        Set<MetaEntity> activeMeasures = addExistingRuntimeEntitiesTo(existingRuntimeEntities, source, result);
        for (MetaEntity metaEntity : source) {
            if (!activeMeasures.contains(metaEntity)) {
                Constructor<?> c = null;
                for (Constructor<?> constructor : cls.getConstructors()) {
                    if (constructor.getParameterTypes().length == 2 && constructor.getParameterTypes()[0].isInstance(metaEntity) && constructor.getParameterTypes()[1].isInstance(parent)) {
                        c = constructor;
                        break;
                    }
                }
                try {
                    Object newInstance = c.newInstance(metaEntity, parent);
                    result.add((T) newInstance);
                    entityManager.persist(newInstance);
                    entityManager.flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;

    }

    protected <T extends RuntimeEntity> Set<MetaEntity> addExistingRuntimeEntitiesTo(Collection<? extends T> existingRuntimeEntities, Collection<? extends MetaEntity> equivalentMetaEntities, Set<T> result) {
        Set<MetaEntity> activeMeasures = new HashSet<MetaEntity>();
        for (T runtimeEntity : existingRuntimeEntities) {
            if (equivalentMetaEntities.contains(runtimeEntity.getMetaEntity())) {
                result.add(runtimeEntity);
                activeMeasures.add(runtimeEntity.getMetaEntity());
            } else if (runtimeEntity instanceof ActivatableRuntimeEntity) {
                ((ActivatableRuntimeEntity) runtimeEntity).setActive(false);
            }
        }
        return activeMeasures;
    }

    //!!!! FOr tests only
    public void flush(){
        entityManager.flush();
    }

    protected RolePerformance findOrCreateRole(Participant participant, RoleInNetwork roleInNetwork) {
        Query q = entityManager.createQuery("select rp from RolePerformance rp where rp.participant=:participant and rp.role=:role");
        q.setParameter("participant", participant);
        q.setParameter("role", roleInNetwork);
        List<RolePerformance> resultList = q.getResultList();
        RolePerformance rp = null;
        if (resultList.isEmpty()) {
            rp = new RolePerformance(roleInNetwork, participant);
            entityManager.persist(rp);
            entityManager.flush();
        } else {
            rp = resultList.get(0);
        }
        Collection<ValuePropositionPerformance> pvpp = syncRuntimeEntities(rp.getProvidedValuePropositions(), rp.getRole().getProvidedValuePropositions(), ValuePropositionPerformance.class, rp);
        for (ValuePropositionPerformance p : pvpp) {
            Collection<ValuePropositionComponentPerformance> cs = syncRuntimeEntities(p.getComponents(), p.getValueProposition().getComponents(), ValuePropositionComponentPerformance.class, p);
            for (ValuePropositionComponentPerformance cc : cs) {
                syncRuntimeEntities(cc.getMeasurements(), cc.getValuePropositionComponent().getMeasures(), ValuePropositionComponentPerformanceMeasurement.class, cc);
            }
        }
        return rp;
    }
}