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

    public void applyResourceAssignments(CollaborationInstance ci) {
        for (ActivityInstance ai : ci.getActivities()) {
            assignRoleResource(ai);
        }
        for (SupplyingStoreInstance ssi : ci.getSupplyingStores()) {
            assignRoleResource(ssi);
        }
    }

    protected void assignRoleResource(PortContainerInstance pci) {
        ReusableBusinessItemPerformance roleResource = pci.findProvidingRoleResource();
        if (roleResource != null) {
            IndividualParticipant participant = roleResource.getRepresents();
            if (pci instanceof ActivityInstance) {
                ActivityInstance ai = (ActivityInstance) pci;
                assignActivityToParticipant(participant, ai);
            } else {
                SupplyingStoreInstance ssi = (SupplyingStoreInstance) pci;
                assignStoreToParticipant(participant, ssi);
            }
        }
    }

    public RolePerformance assignActivityToParticipant(Participant participant, ActivityInstance ai) {
        CapabilityOffer capabilityOffer = findOrCreateCapabilityOffer(participant, ai.getActivity().getCapabilityRequirement());
        RolePerformance rolePerformance = findOrCreateRole(participant, ai.getResponsibleRole().getFulfillingNetworkRole());
        return assignActivity(ai, capabilityOffer, rolePerformance);
    }

    private RolePerformance assignActivity(ActivityInstance ai, CapabilityOffer capabilityOffer, RolePerformance rolePerformance) {
        ai.setCapabilityOffer(capabilityOffer);
        ai.setPerformingRole(rolePerformance);
        return ai.getPerformingRole();
    }

    public RolePerformance assignStoreToParticipant(Participant participant, SupplyingStoreInstance ssi) {
        StorePerformance storePerformance = findOrCreateStorePerformance(participant, ssi.getSupplyingStore().getStoreRequirement());
        RolePerformance rolePerformance = findOrCreateRole(participant, ssi.getResponsibleRole().getFulfillingNetworkRole());
        return assignSupplyingStore(ssi, storePerformance, rolePerformance);
    }

    private RolePerformance assignSupplyingStore(SupplyingStoreInstance ssi, StorePerformance storePerformance, RolePerformance rolePerformance) {
        ssi.setStore(storePerformance);
        ssi.setSupplyingRole(rolePerformance);
        return ssi.getSupplyingRole();
    }


    public void assignToRoles(CollaborationInstance observation, Collection<RolePerformance> rolePerformances) {
        for (Role tmp : observation.getCollaboration().getCollaborationRoles()) {
            RoleInCapabilityMethod role = (RoleInCapabilityMethod) tmp;
            RolePerformance rp = findMatchingRolePerformance(rolePerformances, role.getFulfillingNetworkRole());
            if (rp != null) {
                observation.getCollaborationRoles().add(rp);
                Participant participant = rp.getParticipant();
                for (Activity activity : role.getPerformedActitivities()) {
                    CapabilityOffer cp = findOrCreateCapabilityOffer(participant, activity.getCapabilityRequirement());
                    for (ActivityInstance ao : observation.findActivities(activity)) {
                        assignActivity(ao, cp, rp);
                    }
                }
                for (SupplyingStore ss : role.getSupplyingStores()) {
                    StorePerformance sp = findOrCreateStorePerformance(participant, ss.getStoreRequirement());
                    for (SupplyingStoreInstance sso : observation.findSupplyingStores(ss)) {
                        assignSupplyingStore(sso, sp, rp);
                    }
                }
            }
        }
        syncValuePropositions(observation);
        entityManager.flush();
    }

    private StorePerformance findOrCreateStorePerformance(Participant participant, StoreDefinition storeRequirement) {
        Query q = entityManager.createQuery("select sp from StorePerformance sp where sp.owner= :participant and sp.storeDefinition=:storeDefinition");
        q.setParameter("participant", participant);
        q.setParameter("storeDefinition", storeRequirement);
        List<StorePerformance> result = q.getResultList();
        StorePerformance sp;
        if (result.isEmpty()) {
            if (storeRequirement instanceof PoolDefinition) {
                sp = new PoolPerformance((PoolDefinition) storeRequirement, participant);
            } else {
                sp = new StorePerformance(storeRequirement, participant);
            }
            entityManager.persist(sp);
        } else {
            sp = result.get(0);
        }
        sp.setActive(true);
        syncRuntimeEntities(sp.getMeasurements(), sp.getStoreDefinition().getMeasures(), StoreMeasurement.class, sp);
        return sp;
    }

    private CapabilityOffer findOrCreateCapabilityOffer(Participant participant, Capability capabilityRequirement) {
        Query q = entityManager.createQuery("select cp from CapabilityOffer cp where cp.participant= :participant and cp.capability=:capability");
        q.setParameter("participant", participant);
        q.setParameter("capability", capabilityRequirement);
        List<CapabilityOffer> result = q.getResultList();
        CapabilityOffer cp;
        if (result.isEmpty()) {
            cp = new CapabilityOffer(capabilityRequirement, participant);
            entityManager.persist(cp);
        } else {
            cp = result.get(0);
        }
        cp.setActive(true);
        syncRuntimeEntities(cp.getMeasurements(), cp.getCapability().getMeasures(), CapabilityMeasurement.class, cp);
        return cp;
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
                    RolePerformance providerPerformance = findMatchingRolePerformance(c.getCollaborationRoles(), ((RoleInCapabilityMethod) provider).getFulfillingNetworkRole());
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

    public void assignToBusinessItem(BusinessItemObservation observation, ReusableBusinessItemPerformance rbip) {
        observation.setSharedReference(rbip);
        for (DeliverableFlowInstance dfi : observation.getDeliverableFlows()) {
            if (rbip.equals(dfi.getTargetPortContainer().findProvidingRoleResource())) {
                assignRoleResource(dfi.getTargetPortContainer());
            }
        }
    }
}
