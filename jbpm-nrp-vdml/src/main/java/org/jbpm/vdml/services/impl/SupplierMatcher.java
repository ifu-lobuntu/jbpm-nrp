package org.jbpm.vdml.services.impl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemRequirement;
import org.jbpm.vdml.services.api.model.LocationCriterion;
import org.jbpm.vdml.services.api.model.MeasurementCriterion;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * 3. As a consumer, I would like to find suppliers offering stores/pools/capabilities based on the immediate requirements of the exchange (time, place, quantity and quality requirements) so that we can exchange value with each other.
 * 5. As a project custodian, I would like to find suppliers for a single activity/pool/store on a project to maximise chances of success of the activity on the project.
 * 7. As a project custodian, I would like to find suppliers  that can take responsibilities for all activities/pools/stores associated with a certain role on the project to maximise chances of success of the entire project.
 */
public class SupplierMatcher extends AbstractRuntimeService {

    public SupplierMatcher() {

    }

    public SupplierMatcher(EntityManager entityManager) {
        super(entityManager);
    }

    public List<StorePerformance> findMatchingStore(String storeDef, LocationCriterion location, ReusableBusinessItemRequirement a, Collection<MeasurementCriterion> criteria) {
        return null;
    }

    public List<PoolPerformance> findMatchingPool(String poolDef, LocationCriterion location, ReusableBusinessItemRequirement a, Collection<MeasurementCriterion> criteria) {
        return null;
    }

    public List<CapabilityOffer> findMatchingCapabilityOffer(String capabilityUri, ReusableBusinessItemRequirement location, Collection<MeasurementCriterion> criteria) {
        String s = CriteriaUtil.buildCriteriaString(criteria);
        Query q = entityManager.createQuery("select distinct m.capabilityOffer from CapabilityMeasurement m " +
                        "where m.capabilityOffer.capability.uri = :capabilityUri and " +
                        " distance(m.capabilityOffer.participant.address.location, :desiredLocation) < :maxDistance and  " +
                        s +
                        " group by  m.capabilityOffer having count (m) >= :numberOfCriteria"
        );
        q.setParameter("capabilityUri", capabilityUri);
        q.setParameter("maxDistance", LocationUtil.meterToEstimatedDegrees(location.getMaxDistanceInMeter()));
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        final Point desiredLocation = geometryFactory.createPoint(new Coordinate(location.getLongitude(), location.getLattitude()));
        q.setParameter("desiredLocation", desiredLocation);
        q.setParameter("numberOfCriteria", (long)criteria.size());
        List<CapabilityOffer> resultList = q.getResultList();
        Collections.sort(resultList, new Comparator<CapabilityOffer>() {
            @Override
            public int compare(CapabilityOffer o1, CapabilityOffer o2) {
                Double distance1 = o1.getParticipant().getAddress().getLocation().distance(desiredLocation);
                Double distance2 = o2.getParticipant().getAddress().getLocation().distance(desiredLocation);
                return distance1.compareTo(distance2);
            }
        });
        return resultList;
    }
    public List<ValuePropositionPerformance> findMatchingValueProposition(String capabilityDef, ReusableBusinessItemRequirement location, Collection<MeasurementCriterion> criteria){
        return null;
    }
}
