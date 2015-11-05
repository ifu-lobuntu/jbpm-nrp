package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

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


    public void assignToRoles(CollaborationInstance observation, Collection<RolePerformance> rolePerformances) {
        for (RolePerformance rp : rolePerformances) {
            observation.getCollaborationRoles().add(rp);
            for (Activity activity : rp.getRole().getPerformedActitivities()) {
                ActivityInstance ao = observation.findFirstActivity(activity);
                Query q = entityManager.createQuery("select cp from CapabilityOffer cp where cp.participant= :participant and cp.capability=:capability");
                q.setParameter("participant", rp.getParticipant());
                q.setParameter("capability", activity.getCapabilityRequirement());
                List<CapabilityOffer> result = q.getResultList();
                CapabilityOffer cp;
                if (result.isEmpty()) {
                    cp = new CapabilityOffer(activity.getCapabilityRequirement(), rp.getParticipant());
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
                SupplyingStoreInstance sso = observation.findSupplyingStore(ss);
                Query q = entityManager.createQuery("select sp from StorePerformance sp where sp.owner= :participant and sp.storeDefinition=:storeDefinition");
                q.setParameter("participant", rp.getParticipant());
                q.setParameter("storeDefinition", ss.getStoreRequirement());
                List<StorePerformance> result = q.getResultList();
                StorePerformance sp;
                if (result.isEmpty()) {
                    if (ss.getStoreRequirement() instanceof PoolDefinition) {
                        sp = new PoolPerformance((PoolDefinition) ss.getStoreRequirement(), rp.getParticipant());
                    } else {
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
        entityManager.flush();
    }

    private void syncValuePropositions(CollaborationInstance c) {
        for (ValuePropositionInstance vpi : c.getValuePropositions()) {
            vpi.setActive(false);
        }
        for (RolePerformance provider : c.getCollaborationRoles()) {
            for (ValueProposition vp : provider.getRole().getProvidedValuePropositions()) {
                for (RolePerformance recipient : c.findRolePerformances(vp.getRecipient())) {
                    Collection<PortContainerInstance> responsibilitiesToRecipient = findResponsibilitiesTo(c, provider, recipient);
                    if (responsibilitiesToRecipient.size() > 0) {
                        ValuePropositionInstance vpi = c.findValuePropositionInstance(provider, recipient);
                        if (vpi == null) {
                            vpi = new ValuePropositionInstance(c, vp, provider, recipient);
                        } else {
                            vpi.setActive(true);
                        }
                        syncRuntimeEntities(vpi.getComponents(),vpi.getValueProposition().getComponents(),ValuePropositionComponentInstance.class,vpi);
                        setValueAddsAggregatedFrom(vpi, responsibilitiesToRecipient);
                    }
                }
            }
        }
    }

    private void setValueAddsAggregatedFrom(ValuePropositionInstance vpi, Collection<PortContainerInstance> responsibilitiesToRecipient) {
        for (ValuePropositionComponentInstance vpci : vpi.getComponents()) {
            ValuePropositionComponent vpc = vpci.getValuePropositionComponent();
            syncRuntimeEntities(vpci.getMeasurements(), vpc.getMeasures(), ValueElementInstanceMeasurement.class, vpci, vpc.getPercentageWeight(),vpc.getSatisfactionLevel(),vpc.getValueMeasure());
            vpci.removeAllValueAddsAggregatedFrom();
            Set<ValueAdd> valueAddsAggregatedFrom = vpc.getValueAddsAggregatedFrom();
            for (PortContainerInstance pci : responsibilitiesToRecipient) {
                Set<ValueAddInstance> vais = pci.findValueAdds(valueAddsAggregatedFrom);
                vpci.addValueAddsAggregatedFrom(vais);
            }
        }
    }


    private Collection<PortContainerInstance> findResponsibilitiesTo(CollaborationInstance ci, RolePerformance provider, RolePerformance recipient) {
        Collection<PortContainerInstance> result = new HashSet<PortContainerInstance>();
        for (PortContainerInstance potentialResponsibility : ci.findResponsibilities(provider)) {
            if (outputsReachRolePerformance(potentialResponsibility, recipient) || inputsReachRolePerformance(potentialResponsibility, recipient)) {
                result.add(potentialResponsibility);
            }
        }
        return result;
    }

    private boolean inputsReachRolePerformance(PortContainerInstance pci1, RolePerformance rp2) {
        for (InputPortInstance ipi : pci1.getInputPorts()) {
            DeliverableFlowInstance input = ipi.getInput();
            if (input != null) {
                if (input.getSourcePortContainer().getResponsibleRolePerformance() != null && input.getSourcePortContainer().getResponsibleRolePerformance().equals(rp2)) {
                    return true;
                } else {
                    if (inputsReachRolePerformance(input.getSourcePortContainer(), rp2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean outputsReachRolePerformance(PortContainerInstance pci1, RolePerformance rp2) {
        for (OutputPortInstance opi : pci1.getOutputPorts()) {
            DeliverableFlowInstance output = opi.getOutput();
            if (output != null) {
                if (output.getTargetPortContainer().getResponsibleRolePerformance() != null && output.getTargetPortContainer().getResponsibleRolePerformance().equals(rp2)) {
                    return true;
                } else {
                    if (outputsReachRolePerformance(output.getTargetPortContainer(), rp2)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private void collectRecipients(ValueProposition vp, Set<RolePerformance> recipients, PortContainerInstance pci) {
        for (OutputPortInstance opi : pci.getOutputPorts()) {
            if (opi.getOutput() != null) {
                if (opi.getOutput().getTargetPortContainer().getResponsibleRolePerformance() != null) {
                    if (opi.getOutput().getTargetPortContainer().getResponsibleRolePerformance().getRole().equals(vp.getRecipient())) {
                        recipients.add(opi.getOutput().getTargetPortContainer().getResponsibleRolePerformance());
                    }
                }
                //TODO validate against circular flows
                collectRecipients(vp, recipients, opi.getOutput().getTargetPortContainer());
            }
        }
    }

    public void assignToActivities(ActivityInstance observation, CapabilityOffer capability) {
        observation.setCapabilityOffer(capability);
        observation.setPerformingRole(findOrCreateRole(capability.getParticipant(), observation.getActivity().getPerformingRole()));
        //TODO signal event to commence ConversationForAction
    }

    public void assignToSupplyingStores(SupplyingStoreInstance observation, StorePerformance capability) {
        observation.setStore(capability);
        observation.setSupplyingRole(findOrCreateRole(capability.getOwner(), observation.getSupplyingStore().getSupplyingRole()));
        //TODO signal event to commence ConversationForAction
    }

    public void assignToBusinessItem(BusinessItemObservation observation, ReusableBusinessItemPerformance storePerformance) {
        observation.setSharedReference(storePerformance);
        //uhm yeah well ok?!
    }
}
