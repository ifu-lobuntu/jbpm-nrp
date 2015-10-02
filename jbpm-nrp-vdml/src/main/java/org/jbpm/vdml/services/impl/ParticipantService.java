package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.api.model.LinkedExternalObject;
import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.jbpm.vdml.services.impl.model.scheduling.Schedule;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * 1. As a participant, I would like to register with the platform so that I can transact on it.
 * 2. As a supplier, I would like to register the Stores/Pools/Capabilities that I offer so that other participants can use it.
 */
public class ParticipantService extends AbstractRuntimeService {

    public ParticipantService() {
    }

    public ParticipantService(EntityManager entityManager) {
        super(entityManager);
    }
    //TODO:
    //findParticipantByValueProposition(valuePropositionId, Collection<MeasurementCriterion>)
    //findParticipantByCapability(capabilityId, Collection<MeasurementCriterion>)
    //findParticipantByStore(storeDefId, Collection<MeasurementCriterion>)
    //connect(participantId,Collection<Long> selectedValuePropositionPerformances);


    public Participant findParticipant(Long id) {
        return entityManager.find(Participant.class, id);
    }

    public IndividualParticipant createIndividualParticipant(String userName) {
        IndividualParticipant entity = new IndividualParticipant(userName);
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    public IndividualParticipant findIndividualParticipant(String userName) {
        Query q = entityManager.createQuery("select  rp from IndividualParticipant rp where rp.userName=:userName");
        q.setParameter("userName", userName);
        return (IndividualParticipant) q.getSingleResult();
    }

    public void setRoles(Long participantId, Collection<String> roleIds) {
        Collection<Role> storeDefinitions = entityManager.createQuery("select c from Role c where c.uri in :roleIds").setParameter("roleIds", roleIds).getResultList();
        Participant participant = entityManager.find(Participant.class, participantId);
        syncRuntimeEntities(participant.getRolePerformances(), storeDefinitions, RolePerformance.class, participant);
        for (RolePerformance cp : participant.getRolePerformances()) {
            Collection<ValuePropositionPerformance> pvpp = syncRuntimeEntities(cp.getProvidedValuePropositions(), cp.getRole().getProvidedValuePropositions(), ValuePropositionPerformance.class, cp);
            for (ValuePropositionPerformance p : pvpp) {
                Collection<ValuePropositionComponentPerformance> cs = syncRuntimeEntities(p.getComponents(), p.getValueProposition().getComponents(), ValuePropositionComponentPerformance.class, p);
                for (ValuePropositionComponentPerformance cc : cs) {
                    syncRuntimeEntities(cc.getMeasurements(), cc.getValuePropositionComponent().getMeasures(), ValuePropositionComponentMeasurement.class, cc);
                }
            }
        }
    }

    public void setStores(Long participantId, Collection<String> storeDefIds) {
        Collection<StoreDefinition> storeDefinitions = entityManager.createQuery("select c from StoreDefinition c where c.uri in :storeDefIds").setParameter("storeDefIds", storeDefIds).getResultList();
        Participant participant = entityManager.find(Participant.class, participantId);
        Set<StorePerformance> result = new HashSet<StorePerformance>();
        Set<MetaEntity> activeStores = addExistingRuntimeEntitiesTo(participant.getOfferedStores(), storeDefinitions, result);
        for (MetaEntity metaEntity : storeDefinitions) {
            if (!activeStores.contains(metaEntity)) {
                StorePerformance newInstance = null;
                if(metaEntity instanceof PoolDefinition){
                    newInstance=new PoolPerformance((PoolDefinition) metaEntity,participant);
                }else{
                    newInstance=new StorePerformance((StoreDefinition) metaEntity,participant);
                }
                result.add( newInstance);
                entityManager.persist(newInstance);
                entityManager.flush();
            }
        }

        for (StorePerformance cp : participant.getOfferedStores()) {
            if (cp.getAddress() == null) {
                cp.setAddress(participant.getAddress());
            }
            syncRuntimeEntities(cp.getMeasurements(), cp.getStoreDefinition().getMeasures(), StoreMeasurement.class, cp);
        }
        entityManager.flush();
    }

    public Long addResourceToStore(Long storeId, LinkedExternalObject leo) {
        StorePerformance store = entityManager.find(StorePerformance.class, storeId);
        BusinessItemDefinition bid = entityManager.find(BusinessItemDefinition.class, leo.getMetaEntityUri());
        ReusableBusinessItemPerformance bip = new ReusableBusinessItemPerformance(bid, new ExternalObjectReference(leo.getObjectType(), leo.getIdentifier()));
        entityManager.persist(bip);
        if (store instanceof PoolPerformance) {
            bip.setHostingPool((PoolPerformance) store);
            bip.setAddress(store.getAddress());
            Schedule schedule = new Schedule();
            entityManager.persist(schedule);
            bip.setSchedule(schedule);
        }
        syncRuntimeEntities(bip.getMeasurements(), bid.getAggregatingMeasures(), ReusableBusinessItemMeasurement.class, bip);
        entityManager.flush();
        return bip.getId();
    }

    public void setCapabilities(Long participantId, Collection<String> capabilityIds) {
        Collection<Capability> capabilities = entityManager.createQuery("select c from Capability c where c.uri in :capabilityIds").setParameter("capabilityIds", capabilityIds).getResultList();
        Participant participant = entityManager.find(Participant.class, participantId);
        syncRuntimeEntities(participant.getCapabilityOffers(), capabilities, CapabilityOffer.class, participant);
        for (CapabilityOffer cp : participant.getCapabilityOffers()) {
            syncRuntimeEntities(cp.getMeasurements(), cp.getCapability().getMeasures(), CapabilityMeasurement.class, cp);
        }
    }

    //TODO figure out what to do here
    public void setAddress(Long id, Address address) {
        Participant participant = entityManager.find(Participant.class, id);
        entityManager.persist(address);
        participant.setAddress(address);
        entityManager.flush();
    }

    //TODO figure out what to do here
    public void setResourceSchedule(Long id, Schedule schedule) {
        ReusableBusinessItemPerformance resource = entityManager.find(ReusableBusinessItemPerformance.class, id);
        entityManager.persist(schedule);
        resource.setSchedule(schedule);
        entityManager.flush();
    }
    //TODO figure out what to do here
    public void setPoolSchedule(Long id, Schedule schedule) {
        PoolPerformance pool = entityManager.find(PoolPerformance.class, id);
        entityManager.persist(schedule);
        pool.setSchedule(schedule);
        entityManager.flush();
    }
}
