package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.api.model.LocationCriterion;
import org.jbpm.vdml.services.api.model.MeasurementCriterion;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

/**
 * 7. As a project custodian, I would like to find suppliers  that can take responsibilities for all activities/pools/stores associated with a certain role on the project to maximise chances of success of the entire project.
 * 12. As a participant, I would like to establish a trust relationships with a potential supplier to make it easier for me to involve them in future projects.
 */
public class TrustRelationshipService extends AbstractRuntimeService {

    public TrustRelationshipService() {

    }

    public TrustRelationshipService(EntityManager entityManager) {
        super(entityManager);
    }

    public List<ValuePropositionPerformance> findMatchingValueProposition(String valuePropositionRef, LocationCriterion location, Collection<MeasurementCriterion> criteria){
        return null;
    }
    public List<RelationshipPerformance> findMyPreferredSuppliers(String valuePropositionRef, Long participantId){
        Query q = entityManager.createQuery("select rp from RelationshipPerformance rp where rp.recipient.participant.id =:participantId and rp.valueProposition.uri=:valuePropositionUri");
        q.setParameter("participantId",participantId);
        q.setParameter("valuePropositionUri", valuePropositionRef);
        return q.getResultList();
    }
    public void requestTrustRelationship(String valuePropositionRef, Long requesterId,Long providerId){
        ValueProposition valueProposition=entityManager.find(ValueProposition.class,valuePropositionRef);
        RolePerformance from = findOrCreateRole(entityManager.find(Participant.class, providerId), valueProposition.getProvider());
        RolePerformance to = findOrCreateRole(entityManager.find(Participant.class, requesterId), valueProposition.getRecipient());
        RelationshipPerformance rp = new RelationshipPerformance(valueProposition, from);
        rp.setRecipient(to);
        entityManager.persist(rp);
        Collection<RelationshipComponentPerformance> components = syncRuntimeEntities(rp.getComponents(), rp.getValueProposition().getComponents(), RelationshipComponentPerformance.class, rp);
        for (RelationshipComponentPerformance component : components) {
            syncRuntimeEntities(component.getMeasurements(), component.getValuePropositionComponent().getMeasures(),RelationshipComponentMeasurement.class,component);
        }
        entityManager.flush();
    }
    public void confirmTrustRelationship(RelationshipPerformance relationship){

    }
}
