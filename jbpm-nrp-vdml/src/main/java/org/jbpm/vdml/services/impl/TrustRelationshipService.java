package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.api.model.LocationCriterion;
import org.jbpm.vdml.services.api.model.MeasurementCriterion;
import org.jbpm.vdml.services.impl.model.runtime.ProvidedValuePropositionPerformance;
import org.jbpm.vdml.services.impl.model.runtime.ValuePropositionPerformance;

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

    public List<ProvidedValuePropositionPerformance> findMatchingValueProposition(String valuePropositionRef, LocationCriterion location, Collection<MeasurementCriterion> criteria){
        return null;
    }
    public List<ValuePropositionPerformance> findMyPreferredSuppliers(String valuePropositionRef, Long participantId){
        Query q = entityManager.createQuery("select vpp from ValuePropositionPerformance  vpp where vpp.receiver.participant.id =:participantId and vpp.valueProposition.uri=:valuePropositionUri");
        q.setParameter("participantId",participantId);
        q.setParameter("valuePropositionUri", valuePropositionRef);
        return q.getResultList();
    }
    public void requestTrustRelationship(String valuePropositionRef, ProvidedValuePropositionPerformance supplier){

    }
    public void confirmTrustRelationship(ValuePropositionPerformance relationship){

    }
}
