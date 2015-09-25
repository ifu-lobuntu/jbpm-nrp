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


    public CapabilityPerformance findCapabilityPerformance(Long cpId) {
        return entityManager.find(CapabilityPerformance.class, cpId);
    }

    public void calculateCapabilityPerformance(Long cpId) {
        CapabilityPerformance measurand = entityManager.find(CapabilityPerformance.class, cpId);
        Map<String, Measurement> context = new HashMap<String, Measurement>();
        addToContext(context, measurand.getMeasurements());
        Set<Measurement> otherMeasurements = new MeasurementAggregator(entityManager).resolveAggregatedMEasurements(measurand, "ActivityMeasurement m where m.activity.capabilityOffer");
        resolveMeasurements(measurand.getCapability().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }


    public void calculateStorePerformance(Long spId) {
        StorePerformance measurand = entityManager.find(StorePerformance.class, spId);
        Map<String, Measurement> context = new HashMap<String, Measurement>();
        addToContext(context, measurand.getMeasurements());
        Set<Measurement> otherMeasurements = new MeasurementAggregator(entityManager).resolveAggregatedMEasurements(measurand, "SupplyingStoreMeasurement m where m.store.store");
        resolveMeasurements(measurand.getStoreDefinition().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }

    public StorePerformance findStorePerformance(Long spId) {
        return entityManager.find(StorePerformance.class, spId);
    }

    public void calculateReusableResourcePerformance(Long bipId) {
        ReusableBusinessItemPerformance measurand = entityManager.find(ReusableBusinessItemPerformance.class, bipId);
        Map<String, Measurement> context = new HashMap<String, Measurement>();
        addToContext(context, measurand.getMeasurements());
        Set<Measurement> otherMeasurements = new MeasurementAggregator(entityManager).resolveAggregatedMEasurements(measurand, "BusinessItemMeasurement m where m.businessItem.instanceReference");
        resolveMeasurements(measurand.getDefinition().getDeploymentId(), context, otherMeasurements);
        entityManager.flush();
    }


    public void calculateValueProposition(Long pvppId) {
        ValuePropositionPerformance vpp = entityManager.find(ValuePropositionPerformance.class, pvppId);
        String deploymentId = vpp.getValueProposition().getCollaboration().getDeploymentId();
        for (final ValuePropositionComponentPerformance measurand : vpp.getComponents()) {
            Map<String, Measurement> context = new HashMap<String, Measurement>();
            addToContext(context, measurand.getMeasurements());
            Set<Measurement> otherMeasurements = new MeasurementAggregator<ValuePropositionComponentPerformance>(entityManager){
                @Override
                protected void addAdditionalParameters(Query q,ValuePropositionComponentPerformance measurand) {
                    q.setParameter("measurand", measurand.getValueProposition().getProvider());
                }
            }.resolveAggregatedMEasurements(measurand, "ValueAddMeasurement m where m.deliverableFlow.sourcePortContainer.responsibleRolePerformance ");
            resolveMeasurements(deploymentId, context, otherMeasurements);
        }
        entityManager.flush();
    }


    public void calculateRelationshipPerformance(Long rpId) {
        RelationshipPerformance vpp = entityManager.find(RelationshipPerformance.class, rpId);
        String deploymentId = vpp.getValueProposition().getCollaboration().getDeploymentId();
        for (final RelationshipComponentPerformance measurand : vpp.getComponents()) {
            Map<String, Measurement> context = new HashMap<String, Measurement>();
            addToContext(context, measurand.getMeasurements());
            Set<Measurement> otherMeasurements = new MeasurementAggregator<RelationshipComponentPerformance>(entityManager){
                @Override
                protected void addAdditionalParameters(Query q,RelationshipComponentPerformance measurand) {
                    q.setParameter("measurand", measurand.getRelationship().getProvider());
                    q.setParameter("receiver", measurand.getRelationship().getRecipient());
                }
            }.resolveAggregatedMEasurements(measurand, "ValueAddMeasurement m where m.deliverableFlow.targetPortContainer.responsibleRolePerformance = :receiver and m.deliverableFlow.sourcePortContainer.responsibleRolePerformance ");
            resolveMeasurements(deploymentId, context, otherMeasurements);
        }
        entityManager.flush();
    }

    public ReusableBusinessItemPerformance findReusableBusinessItemPerformance(Long bipId) {
        return entityManager.find(ReusableBusinessItemPerformance.class, bipId);
    }

    public RelationshipPerformance findRelationshipPerformance(Long id) {
        return entityManager.find(RelationshipPerformance.class,id);
    }

    public ValuePropositionPerformance findValuePropositionPerformance(Long id) {
        return entityManager.find(ValuePropositionPerformance.class,id);
    }
}
