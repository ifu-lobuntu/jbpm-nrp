package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.api.model.ReusableBusinessItemRequirement;
import org.jbpm.vdml.services.api.model.LocationCriterion;
import org.jbpm.vdml.services.api.model.MeasurementCriterion;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

    public List<CapabilityPerformance> findMatchingCapabilityOffer(String capabilityDef, LocationCriterion location, ReusableBusinessItemRequirement a, Collection<MeasurementCriterion> criteria) {
        return null;
    }
    public List<ProvidedValuePropositionPerformance> findMatchingValueProposition(String capabilityDef, LocationCriterion location, Date from, Date to, Collection<MeasurementCriterion> criteria){
        return null;
    }
}
