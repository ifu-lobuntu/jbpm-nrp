package org.jbpm.vdml.services.impl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jbpm.vdml.services.api.model.LocationCriterion;
import org.jbpm.vdml.services.api.model.MeasurementCriterion;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

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

    public List<ValuePropositionPerformance> findMatchingValueProposition(String valuePropositionRef, LocationCriterion location, Collection<MeasurementCriterion> criteria) {
        String s = CriteriaUtil.buildCriteriaString(criteria);
        Query q = entityManager.createQuery("select distinct m.component.valueProposition from ValuePropositionComponentMeasurement  m " +
                        "where m.component.valueProposition.valueProposition.uri = :valuePropositionUri and " +
                        " distance(m.component.valueProposition.provider.participant.address.location, :desiredLocation) < :maxDistance and  " +
                        s +
                        " group by  m.component.valueProposition having count (m) >= :numberOfCriteria"
        );
        q.setParameter("valuePropositionUri", valuePropositionRef);
        q.setParameter("maxDistance", LocationUtil.meterToEstimatedDegrees(location.getDistance()));
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        final Point desiredLocation = geometryFactory.createPoint(new Coordinate(location.getLongitude(), location.getLattitude()));
        q.setParameter("desiredLocation", desiredLocation);
        q.setParameter("numberOfCriteria", (long)criteria.size());
        List<ValuePropositionPerformance> resultList = q.getResultList();
        Collections.sort(resultList, new Comparator<ValuePropositionPerformance>() {
            @Override
            public int compare(ValuePropositionPerformance o1, ValuePropositionPerformance o2) {
                Double distance1 = o1.getProvider().getParticipant().getAddress().getLocation().distance(desiredLocation);
                Double distance2 = o2.getProvider().getParticipant().getAddress().getLocation().distance(desiredLocation);
                return distance1.compareTo(distance2);
            }
        });
        return resultList;
    }

    public List<TrustRelationship> findMyPreferredSuppliers(String valuePropositionRef, Long participantId) {
        Query q = entityManager.createQuery("select rp from RelationshipPerformance rp where rp.recipient.participant.id =:participantId and rp.valueProposition.uri=:valuePropositionUri");
        q.setParameter("participantId", participantId);
        q.setParameter("valuePropositionUri", valuePropositionRef);
        return q.getResultList();
    }

    public void requestTrustRelationship(String valuePropositionRef, Long requesterId, Long providerId) {
        ValueProposition valueProposition = entityManager.find(ValueProposition.class, valuePropositionRef);
        RolePerformance from = findOrCreateRole(entityManager.find(Participant.class, providerId), valueProposition.getProvider());
        RolePerformance to = findOrCreateRole(entityManager.find(Participant.class, requesterId), valueProposition.getRecipient());
        TrustRelationship rp = new TrustRelationship(valueProposition, from);
        rp.setRecipient(to);
        entityManager.persist(rp);
        Collection<TrustRelationshipComponent> components = syncRuntimeEntities(rp.getComponents(), rp.getValueProposition().getComponents(), TrustRelationshipComponent.class, rp);
        for (TrustRelationshipComponent component : components) {
            syncRuntimeEntities(component.getMeasurements(), component.getValuePropositionComponent().getMeasures(), TrustRelationshipComponentMeasurement.class, component);
        }
        entityManager.flush();
    }

    public void confirmTrustRelationship(Long relationshipId) {
        TrustRelationship relationship = entityManager.find(TrustRelationship.class, relationshipId);
        relationship.setStatus(TrustRelationshipStatus.CONFIRMED);
        entityManager.flush();
    }

    public void rejectTrustRelationship(Long relationshipId) {
        TrustRelationship relationship = entityManager.find(TrustRelationship.class, relationshipId);
        relationship.setStatus(TrustRelationshipStatus.REJECTED);
        //TODO - delete it?
        entityManager.flush();
    }
}
