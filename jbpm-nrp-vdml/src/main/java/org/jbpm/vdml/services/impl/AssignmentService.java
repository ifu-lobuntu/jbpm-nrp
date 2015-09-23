package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

/**
 * 6. As a project custodian, I would like assign supplier to an activity/suppliedStore/suppliedPool in project so that the project members can start exchanging value with them.
 * 8. As a project custodian, I would like assign supplier to a specific role in project so that the project members can start exchanging value with them.
 */
public class AssignmentService extends AbstractRuntimeService {

    public AssignmentService() {

    }

    public AssignmentService(EntityManager entityManager) {
        super(entityManager);
    }


    public void assignToRoles(CollaborationObservation observation, Collection<RolePerformance> rolePerformances) {
        for (RolePerformance rp : rolePerformances) {
            observation.getCollaborationRoles().add(rp);
            for (Activity activity : rp.getRole().getPerformedActitivities()) {
                ActivityObservation ao = observation.findActivity(activity);
                Query q = entityManager.createQuery("select cp from CapabilityPerformance cp where cp.participant= :participant and cp.capability=:capability");
                q.setParameter("participant", rp.getParticipant());
                q.setParameter("capability", activity.getCapabilityRequirement());
                List<CapabilityPerformance> result = q.getResultList();
                CapabilityPerformance cp;
                if (result.isEmpty()) {
                    cp = new CapabilityPerformance(activity.getCapabilityRequirement(), rp.getParticipant());
                    entityManager.persist(cp);
                } else {
                    cp = result.get(0);
                }
                cp.setActive(true);
                syncRuntimeEntities(cp.getMeasurements(), cp.getCapability().getMeasures(), CapabilityMeasurement.class, cp);
                ao.setCapabilityOffer(cp);
                ao.setPerformingRole(rp);
                //TODO signal event to commence ConversationForAction
            }
            for (SupplyingStore ss : rp.getRole().getSupplyingStores()) {
                SupplyingStoreObservation sso = observation.findSupplyingStore(ss);
                Query q = entityManager.createQuery("select sp from StorePerformance sp where sp.owner= :participant and sp.storeDefinition=:storeDefinition");
                q.setParameter("participant", rp.getParticipant());
                q.setParameter("storeDefinition", ss.getStoreRequirement());
                List<StorePerformance> result = q.getResultList();
                StorePerformance sp;
                if (result.isEmpty()) {
                    if(ss.getStoreRequirement() instanceof PoolDefinition){
                        sp = new PoolPerformance((PoolDefinition)ss.getStoreRequirement(), rp.getParticipant());
                    }else {
                        sp = new StorePerformance(ss.getStoreRequirement(), rp.getParticipant());
                    }
                    entityManager.persist(sp);
                } else {
                    sp = result.get(0);
                }
                sp.setActive(true);
                syncRuntimeEntities(sp.getMeasurements(), sp.getStoreDefinition().getMeasures(), StoreMeasurement.class, sp);
                sso.setStore(sp);
                sso.setSupplyingRole(rp);
                //TODO signal event to commence ConversationForAction
            }
        }
    }
    public void assignToActivities(ActivityObservation observation, CapabilityPerformance capability) {
        observation.setCapabilityOffer(capability);
        observation.setPerformingRole(findOrCreateRole(capability.getParticipant(), observation.getActivity().getPerformingRole()));
        //TODO signal event to commence ConversationForAction
    }
    public void assignToSupplyingStores(SupplyingStoreObservation observation, StorePerformance capability) {
        observation.setStore(capability);
        observation.setSupplyingRole(findOrCreateRole(capability.getOwner(), observation.getSupplyingStore().getSupplyingRole()));
        //TODO signal event to commence ConversationForAction
    }

}
