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
        for (Role tmp : observation.getCollaboration().getCollaborationRoles()) {
            RoleInCapabilityMethod role = (RoleInCapabilityMethod) tmp;

            RolePerformance rp = findMatchingRolePerformance(rolePerformances, role.getFulfillingNetworkRole());
            if (rp != null) {
                observation.getCollaborationRoles().add(rp);
                for (Activity activity : role.getPerformedActitivities()) {
                    Collection<ActivityInstance> activities = observation.findActivities(activity);
                    for (ActivityInstance ao : activities) {
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
                }
                for (SupplyingStore ss : role.getSupplyingStores()) {
                    for (SupplyingStoreInstance sso : observation.findSupplyingStores(ss)) {
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
            }
        }
        syncValuePropositions(observation);
        entityManager.flush();
    }

    private RolePerformance findMatchingRolePerformance(Collection<RolePerformance> rolePerformances, RoleInNetwork fulfillingNetworkRole) {
        for (RolePerformance rolePerformance : rolePerformances) {
            if (rolePerformance.getRole().equals(fulfillingNetworkRole)) {
                return rolePerformance;
            }
        }
        return null;
    }

    private void syncValuePropositions(CollaborationInstance c) {
        for (ValuePropositionInstance vpi : c.getValuePropositions()) {
            vpi.setActive(false);
        }
        for (Role provider : c.getCollaboration().getCollaborationRoles()) {
            for (ValueProposition vp : provider.getProvidedValuePropositions()) {
                for (RolePerformance recipient : c.findRolePerformances((RoleInCapabilityMethod) vp.getRecipient())) {
                    RolePerformance providerPerformance=findMatchingRolePerformance(c.getCollaborationRoles(), ((RoleInCapabilityMethod) provider).getFulfillingNetworkRole());
                    Collection<PortContainerInstance> responsibilitiesToRecipient = findResponsibilitiesTo(c, providerPerformance, recipient);
                    if (responsibilitiesToRecipient.size() > 0) {
                        ValuePropositionInstance vpi = c.findValuePropositionInstance(providerPerformance, recipient);
                        if (vpi == null) {
                            vpi = new ValuePropositionInstance(c, vp, providerPerformance, recipient);
                        } else {
                            vpi.setActive(true);
                        }
                        syncRuntimeEntities(vpi.getComponents(), vpi.getValueProposition().getComponents(), ValuePropositionComponentInstance.class, vpi);
                        setValueAddsAggregatedFrom(vpi, responsibilitiesToRecipient);
                    }
                }
            }
        }
    }

    private void setValueAddsAggregatedFrom(ValuePropositionInstance vpi, Collection<PortContainerInstance> responsibilitiesToRecipient) {
        for (ValuePropositionComponentInstance vpci : vpi.getComponents()) {
            ValuePropositionComponent vpc = vpci.getValuePropositionComponent();
            syncRuntimeEntities(vpci.getMeasurements(), vpc.getMeasures(), ValuePropositionComponentInstanceMeasurement.class, vpci, vpc.getPercentageWeight(), vpc.getSatisfactionLevel(), vpc.getValueMeasure());
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
            for (DeliverableFlowInstance input : ipi.getInflow()) {
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
            for (DeliverableFlowInstance output : opi.getOutflow()) {
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

    public RolePerformance assignToActivities(ActivityInstance observation, CapabilityOffer capability) {
        observation.setCapabilityOffer(capability);
        observation.setPerformingRole(findOrCreateRole(capability.getParticipant(), observation.getActivity().getPerformingRole().getFulfillingNetworkRole()));
        return observation.getResponsibleRolePerformance();
        //TODO signal event to commence ConversationForAction
    }

    public void assignToSupplyingStores(SupplyingStoreInstance observation, StorePerformance capability) {
        observation.setStore(capability);
        observation.setSupplyingRole(findOrCreateRole(capability.getOwner(), observation.getSupplyingStore().getSupplyingRole().getFulfillingNetworkRole()));
        //TODO signal event to commence ConversationForAction
    }

    public void assignToBusinessItem(BusinessItemObservation observation, ReusableBusinessItemPerformance storePerformance) {
        observation.setSharedReference(storePerformance);
        //uhm yeah well ok?!
    }
}
