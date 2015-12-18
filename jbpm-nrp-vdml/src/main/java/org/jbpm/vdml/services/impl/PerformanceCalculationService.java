package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

public class PerformanceCalculationService extends AbstractCalculationService {

    public PerformanceCalculationService(EntityManager entityManager) {
        super(entityManager);
    }

    public PerformanceCalculationService() {
    }


    public CapabilityOffer findCapabilityPerformance(Long cpId) {
        return entityManager.find(CapabilityOffer.class, cpId);
    }

    public void calculateCapabilityPerformance(Long cpId, ObservationPhase phase) {
        CapabilityOffer measurand = entityManager.find(CapabilityOffer.class, cpId);
        ObservationContext context = new ObservationContext(phase);
        context.putAll(measurand.getMeasurements());
        Set<Measurement> otherMeasurements = new MeasurementAggregator(entityManager).resolveAggregatedMEasurements(measurand, "ActivityMeasurement m where m.activity.capabilityOffer");
        resolveObservedMeasures(measurand.getCapability().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }


    public void calculateStorePerformance(Long spId, ObservationPhase phase) {
        StorePerformance measurand = entityManager.find(StorePerformance.class, spId);
        ObservationContext context = new ObservationContext(phase);
        context.putAll(measurand.getMeasurements());
        Set<Measurement> otherMeasurements = new MeasurementAggregator(entityManager).resolveAggregatedMEasurements(measurand, "SupplyingStoreMeasurement m where m.store.store");
        resolveObservedMeasures(measurand.getStoreDefinition().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }

    public StorePerformance findStorePerformance(Long spId) {
        return entityManager.find(StorePerformance.class, spId);
    }

    public void calculateReusableResourcePerformance(Long bipId, ObservationPhase phase) {
        ReusableBusinessItemPerformance measurand = entityManager.find(ReusableBusinessItemPerformance.class, bipId);
        ObservationContext context = new ObservationContext(phase);
        context.putAll(measurand.getMeasurements());
        Set<Measurement> otherMeasurements = new MeasurementAggregator(entityManager).resolveAggregatedMEasurements(measurand, "BusinessItemMeasurement m where m.businessItem.sharedReference");
        resolveObservedMeasures(measurand.getDefinition().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }


    public void calculateValueProposition(Long pvppId, ObservationPhase phase) {
        ValuePropositionPerformance vpp = entityManager.find(ValuePropositionPerformance.class, pvppId);
        String deploymentId = vpp.getValueProposition().getCollaboration().getDeploymentId();
        for (final ValuePropositionComponentPerformance measurand : vpp.getComponents()) {
            ObservationContext context = new ObservationContext(phase);
            context.putAll(measurand.getMeasurements());
            Set<Measurement> otherMeasurements = new MeasurementAggregator<ValuePropositionComponentPerformance>(entityManager){
                @Override
                protected void addAdditionalParameters(Query q,ValuePropositionComponentPerformance measurand) {
                    q.setParameter("measurand", measurand.getValueProposition().getProvider());
                }
            }.resolveAggregatedMEasurements(measurand, "ValuePropositionComponentInstanceMeasurement m where m.valuePropositionComponent.valueProposition.provider ");
            resolveObservedMeasures(deploymentId, context, otherMeasurements);
        }
        entityManager.flush();
    }


    public void calculateRelationshipPerformance(Long rpId, ObservationPhase phase) {
        TrustRelationship vpp = entityManager.find(TrustRelationship.class, rpId);
        String deploymentId = vpp.getValueProposition().getCollaboration().getDeploymentId();
        for (final TrustRelationshipComponent measurand : vpp.getComponents()) {
            ObservationContext context = new ObservationContext(phase);
            context.putAll(measurand.getMeasurements());
            Set<Measurement> otherMeasurements = new MeasurementAggregator<TrustRelationshipComponent>(entityManager){
                @Override
                protected void addAdditionalParameters(Query q,TrustRelationshipComponent measurand) {
                    q.setParameter("measurand", measurand.getRelationship().getProvider());
                    q.setParameter("receiver", measurand.getRelationship().getRecipient());
                }
            }.resolveAggregatedMEasurements(measurand, "ValuePropositionComponentInstanceMeasurement m where  m.valuePropositionComponent.valueProposition.recipient = :receiver and  m.valuePropositionComponent.valueProposition.provider ");
            resolveObservedMeasures(deploymentId, context, otherMeasurements);
        }
        entityManager.flush();
    }

    public ReusableBusinessItemPerformance findReusableBusinessItemPerformance(Long bipId) {
        return entityManager.find(ReusableBusinessItemPerformance.class, bipId);
    }

    public TrustRelationship findRelationshipPerformance(Long id) {
        return entityManager.find(TrustRelationship.class,id);
    }

    public ValuePropositionPerformance findValuePropositionPerformance(Long id) {
        return entityManager.find(ValuePropositionPerformance.class,id);
    }
}
