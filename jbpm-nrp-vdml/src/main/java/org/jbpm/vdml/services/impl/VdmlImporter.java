package org.jbpm.vdml.services.impl;


import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.meta.Activity;
import org.jbpm.vdml.services.impl.model.meta.Capability;
import org.jbpm.vdml.services.impl.model.meta.CapabilityMethod;
import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.meta.DeliverableFlow;
import org.jbpm.vdml.services.impl.model.meta.InputPort;
import org.jbpm.vdml.services.impl.model.meta.Milestone;
import org.jbpm.vdml.services.impl.model.meta.OutputDelegation;
import org.jbpm.vdml.services.impl.model.meta.OutputPort;
import org.jbpm.vdml.services.impl.model.meta.PoolDefinition;
import org.jbpm.vdml.services.impl.model.meta.Port;
import org.jbpm.vdml.services.impl.model.meta.PortContainer;
import org.jbpm.vdml.services.impl.model.meta.ResourceUseLocation;
import org.jbpm.vdml.services.impl.model.meta.Role;
import org.jbpm.vdml.services.impl.model.meta.StoreDefinition;
import org.jbpm.vdml.services.impl.model.meta.SupplyingStore;
import org.jbpm.vdml.services.impl.model.meta.ResourceUse;
import org.jbpm.vdml.services.impl.model.meta.BusinessItemDefinition;
import org.jbpm.vdml.services.impl.model.meta.ValueAdd;
import org.jbpm.vdml.services.impl.model.meta.ValueElement;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;
import org.jbpm.vdml.services.impl.model.meta.InputDelegation;
import org.jbpm.vdml.services.impl.model.runtime.ExchangeConfiguration;
import org.omg.vdml.CapabilityDefinition;
import org.omg.vdml.CapabilityLibrary;
import org.omg.vdml.ValueDeliveryModel;


import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class VdmlImporter extends MetaBuilder {
    public MeasureBuilder measureBuilder;

    public VdmlImporter() {
    }

    public VdmlImporter(EntityManager entityManager) {
        super(entityManager);
        this.measureBuilder = new MeasureBuilder(entityManager);
    }

    public void buildModel(String deploymentId, org.omg.vdml.ValueDeliveryModel vdm) {

        importCapabilities(deploymentId, vdm);
        importStoreDefinitions(deploymentId, vdm);
        importBusinessItemDefinitions(deploymentId, vdm);
        for (org.omg.vdml.Collaboration collaboration : vdm.getCollaboration()) {
            buildCollaboration(deploymentId, collaboration);
        }
        configureCapabilities(vdm);
        configureStoreDefinitions(vdm);
        for (org.omg.vdml.Collaboration c : vdm.getCollaboration()) {
            linkValueElements(c);
        }
        linkAssignments(vdm);
        linkCapabilityResources(vdm);
        entityManager.flush();
    }

    private void linkCapabilityResources(org.omg.vdml.ValueDeliveryModel vdm) {
        for (org.omg.vdml.CapabilityLibrary cl : vdm.getCapabilitylibrary()) {
            for (org.omg.vdml.Capability capability : cl.getCapability()) {
                if(capability instanceof org.omg.vdml.CapabilityDefinition){
                    org.omg.vdml.CapabilityDefinition from= (org.omg.vdml.CapabilityDefinition)capability;
                    Capability to=find(from,Capability.class);
                    to.clearCapabilityResources();
                    for (org.omg.vdml.BusinessItemDefinition fromBid : from.getCapabilityResourceDefinition()) {
                        BusinessItemDefinition toBid=find(fromBid,BusinessItemDefinition.class);
                        toBid.getSupportedCapabilities().add(to);
                        to.getCapabilityResources().add(toBid);
                    }
                }
            }
        }
    }

    private void linkAssignments(org.omg.vdml.ValueDeliveryModel vdm) {
        for (org.omg.vdml.Collaboration collaboration : vdm.getCollaboration()) {
            for (org.omg.vdml.Role role : collaboration.getCollaborationRole()) {
                for (org.omg.vdml.Assignment assignment : role.getRoleAssignment()) {
                    if (assignment.getParticipant() instanceof org.omg.vdml.Position) {
                        RoleInNetwork roleInNetwork = find(assignment.getParticipant(), RoleInNetwork.class);
                        RoleInCapabilityMethod roleInCapabilityMethod = find(role, RoleInCapabilityMethod.class);
                        roleInCapabilityMethod.setFulfillingNetworkRole(roleInNetwork);
                        roleInNetwork.getFulfilledCapabilityMethodRoles().add(roleInCapabilityMethod);
                    } else if (assignment.getParticipant() instanceof org.omg.vdml.Performer && assignment.eContainer() instanceof org.omg.vdml.DelegationContext) {
                        org.omg.vdml.DelegationContext dc = (org.omg.vdml.DelegationContext) assignment.eContainer();
                        Activity activity = find(dc.getDelegatedActivity(), Activity.class);
                        RoleMapping rm = findOrCreate(assignment,RoleMapping.class,activity);
                        rm.setFromRole(find(assignment.getParticipant(), RoleInCapabilityMethod.class));
                        rm.setToRole(find(assignment.getAssignedRole(), RoleInCapabilityMethod.class));
                    } else if(assignment.getRoleResource().size()==1){
                        Activity activity = find(assignment.getRoleResource().get(0).eContainer(), Activity.class);
                        RoleResource rm = findOrCreate(assignment,RoleResource.class,activity);
                        rm.setFromResource(find(assignment.getRoleResource().get(0), InputPort.class));
                        rm.setToRole(find(assignment.getAssignedRole(), RoleInCapabilityMethod.class));
                    }
                }
            }
        }
    }

    private void configureStoreDefinitions(org.omg.vdml.ValueDeliveryModel vdm) {
        for (org.omg.vdml.StoreLibrary library : vdm.getStoreLibrary()) {
            for (org.omg.vdml.StoreLibraryElement from : library.getStoreLibraryElement()) {
                if (from instanceof org.omg.vdml.StoreDefinition) {
                    StoreDefinition to = find(from, StoreDefinition.class);
                    org.omg.vdml.ExchangeConfiguration fromEc = ((org.omg.vdml.StoreDefinition) from).getExchangeConfiguration();
                    to.setExchangeConfiguration(buildExchangeConfiguration(fromEc));
                }
            }
        }
    }

    private ExchangeConfiguration buildExchangeConfiguration(org.omg.vdml.ExchangeConfiguration fromEc) {
        ExchangeConfiguration ec = null;
        if (fromEc != null) {
            ec = new ExchangeConfiguration();
            ec.setCollaborationToUse(find(fromEc.getExchangeMethod(), CapabilityMethod.class));
            ec.setSupplierRole(find(fromEc.getSupplierRole(), RoleInCapabilityMethod.class));
            ec.setExchangeMilestone(find(fromEc.getExchangeMilestone(), Milestone.class));
            ec.setPoolBooking(find(fromEc.getResourceUseFromPool(), ResourceUse.class));
            entityManager.persist(ec);
            entityManager.flush();
        }
        return ec;
    }

    private void configureCapabilities(org.omg.vdml.ValueDeliveryModel vdm) {
        for (org.omg.vdml.CapabilityLibrary l : vdm.getCapabilitylibrary()) {
            for (org.omg.vdml.Capability from : l.getCapability()) {
                Capability to = find(from, Capability.class);
                org.omg.vdml.ExchangeConfiguration fromEc = from.getExchangeConfiguration();
                to.setExchangeConfiguration(buildExchangeConfiguration(fromEc));
            }
        }
    }

    private void importStoreDefinitions(String deploymentId, org.omg.vdml.ValueDeliveryModel vdm) {
        for (org.omg.vdml.StoreLibrary library : vdm.getStoreLibrary()) {
            for (org.omg.vdml.StoreLibraryElement from : library.getStoreLibraryElement()) {
                if (from instanceof org.omg.vdml.StoreDefinition) {
                    StoreDefinition to = findOrCreate(from, from instanceof org.omg.vdml.PoolDefinition ? PoolDefinition.class : StoreDefinition.class);
                    to.setDeploymentId(deploymentId);
                    to.setResource(findOrCreate(from.getResource(),BusinessItemDefinition.class));
                    to.setName(from.getName());
                    measureBuilder.fromCharacteristics(to.getMeasures(), from.getCharacteristicDefinition());
                }
            }
        }
    }

    private void importCapabilities(String deploymentId, org.omg.vdml.ValueDeliveryModel vdm) {
        for (org.omg.vdml.CapabilityLibrary l : vdm.getCapabilitylibrary()) {
            for (org.omg.vdml.Capability from : l.getCapability()) {
                Capability to = findOrCreate(from, Capability.class);
                to.setName(from.getName());
                to.setDeploymentId(deploymentId);
                measureBuilder.fromCharacteristics(to.getMeasures(), from.getCharacteristicDefinition());
            }
        }
    }

    private void importBusinessItemDefinitions(String deploymentId, org.omg.vdml.ValueDeliveryModel vdm) {
        for (org.omg.vdml.BusinessItemLibrary library : vdm.getBusinessItemLibrary()) {
            for (org.omg.vdml.BusinessItemLibraryElement from : library.getBusinessItemLibraryElement()) {
                BusinessItemDefinition to = findOrCreate(from, BusinessItemDefinition.class);
                to.setName(from.getName());
                to.setDeploymentId(deploymentId);
                if (from instanceof org.omg.vdml.BusinessItemDefinition) {
                    org.omg.vdml.BusinessItemDefinition fromDef = (org.omg.vdml.BusinessItemDefinition) from;
                    to.setFungible(fromDef.getIsFungible());
                    to.setShareable(fromDef.getIsShareable());
                }
                measureBuilder.fromCharacteristics(to.getMeasures(), from.getCharacteristicDefinition());
            }
        }
    }

    public Collaboration buildCollaboration(String deploymentId, org.omg.vdml.Collaboration c) {
        Collaboration result = findOrCreate(c, c instanceof org.omg.vdml.CapabilityMethod ? CapabilityMethod.class : ValueNetwork.class);
        result.setName(c.getName());
        result.setDeploymentId(deploymentId);
        importBusinessItems(c, result);
        importRoles(c, result);
        importPorts(c, result);
        if (c instanceof org.omg.vdml.CapabilityMethod) {
            importActivities(c, result);
            importSupplyingStores(c, result);
            EList<org.omg.vdml.Milestone> milestone = ((org.omg.vdml.CapabilityMethod) c).getMilestone();
            for (org.omg.vdml.Milestone from : milestone) {
                Milestone to = findOrCreate(from, Milestone.class, result);
                to.setName(from.getName());
            }
            importDeliverableFlows(c, result);
            setInitiatingRole(c, (CapabilityMethod) result);
        }
        importValuePropositions(c);
        importPortDelegations(result, c.getInternalPortDelegation());
        importContextBasedPortDelegations(deploymentId, c, result);
        importResourceUses(c);
        entityManager.flush();
        return result;
    }

    private void linkValueElements(org.omg.vdml.Collaboration c) {
        for (org.omg.vdml.SupplyingStore ss : c.getSupplyingStore()) {
            linkValueElements(ss);
        }
        for (org.omg.vdml.Activity activity : c.getActivity()) {
            linkValueElements(activity);
        }
        for (org.omg.vdml.Role r : c.getCollaborationRole()) {
            for (org.omg.vdml.ValueProposition vp : r.getProvidedProposition()) {
                linkValueElements(vp.getComponent());
            }
        }
    }

    private void linkValueElements(org.omg.vdml.PortContainer pc) {
        for (org.omg.vdml.Port p : pc.getContainedPort()) {
            if (p instanceof org.omg.vdml.OutputPort) {
                linkValueElements(((org.omg.vdml.OutputPort) p).getValueAdd());
            }
        }
    }

    private void linkValueElements(EList<? extends org.omg.vdml.ValueElement> veasd) {
        for (org.omg.vdml.ValueElement va : veasd) {
            ValueElement to = find(va, ValueElement.class);
            for (org.omg.vdml.ValueElement ve : va.getAggregatedFrom()) {
                ValueElement otherValueElement = find(ve, ValueElement.class);
                to.getAggregatedFrom().add(otherValueElement);
                otherValueElement.getAggregatedTo().add(otherValueElement);
            }
            for (org.omg.vdml.ValueElement ve : va.getAggregatedTo()) {
                ValueElement otherValueElement = find(ve, ValueElement.class);
                to.getAggregatedTo().add(otherValueElement);
                otherValueElement.getAggregatedFrom().add(otherValueElement);
            }
        }
    }

    protected void setInitiatingRole(org.omg.vdml.Collaboration source, CapabilityMethod result) {
        if (source instanceof org.omg.vdml.CapabilityMethod) {
            org.omg.vdml.CapabilityMethod capabilityMethod = (org.omg.vdml.CapabilityMethod) source;
            org.omg.vdml.Activity initialActivity = capabilityMethod.getInitialActivity();
            if (initialActivity != null) {
                result.setInitiatorRole(result.findRole(initialActivity.getPerformingRole().getName()));
            }
            if (capabilityMethod.getPlanningRole() != null) {
                result.setPlannerRole(result.findRole(capabilityMethod.getPlanningRole().getName()));
            }
        }
        if (result.getInitiatorRole() == null) {
            List<Activity> initiatingActivity = new ArrayList<Activity>();
            for (Activity activity : result.getActivities()) {
                if (activity.getInput().isEmpty() && !activity.getOutput().isEmpty()) {
                    initiatingActivity.add(activity);
                }
            }
            if (initiatingActivity.size() == 1) {
                result.setInitiatorRole(initiatingActivity.get(0).getPerformingRole());
            }
        }
    }

    private void importContextBasedPortDelegations(String deploymentId, org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.Activity activity : c.getActivity()) {
            for (org.omg.vdml.DelegationContext delegationContext : activity.getDelegationContext()) {
                Collaboration contextCollaboration = findCollaboration(buildUri(delegationContext.getContextCollaboration()));
                if (contextCollaboration == null) {
                    buildCollaboration(deploymentId, delegationContext.getContextCollaboration());
                }
                importPortDelegations(result, delegationContext.getContextBasedPortDelegation());
            }
        }
    }

    private void importValuePropositions(org.omg.vdml.Collaboration c) {
        for (org.omg.vdml.Role fromRole : c.getCollaborationRole()) {
            Role role = findRole(buildUri(fromRole));
            for (org.omg.vdml.ValueProposition fromVp : fromRole.getProvidedProposition()) {
                ValueProposition toVp = findOrCreate(fromVp, ValueProposition.class, role, findRole(buildUri(fromVp.getRecipient())));
                toVp.setName(fromVp.getName());
                for (org.omg.vdml.ValuePropositionComponent fromVpc : fromVp.getComponent()) {
                    ValuePropositionComponent toVpc = findOrCreate(fromVpc, ValuePropositionComponent.class, toVp);
                    toVpc.setName(fromVpc.getName());
                    List<org.omg.vdml.MeasuredCharacteristic> measuredCharacteristics = new ArrayList<org.omg.vdml.MeasuredCharacteristic>(fromVpc.getMeasuredCharacteristic());
                    measuredCharacteristics.add(fromVpc.getValueMeasurement());
                    measureBuilder.fromMeasuredCharacteristics(toVpc.getMeasures(), measuredCharacteristics);
                    toVpc.setSatisfactionLevel(measureBuilder.findOrCreateMeasure(fromVpc.getSatisfactionLevel()));
                    toVpc.setPercentageWeight(measureBuilder.findOrCreateMeasure(fromVpc.getPercentageWeight()));
                    toVpc.setValueMeasure(measureBuilder.findOrCreateMeasure(fromVpc.getValueMeasurement()));
                }
            }
        }
    }

    private void importResourceUses(org.omg.vdml.Collaboration c) {
        for (org.omg.vdml.Activity fromActivity : c.getActivity()) {
            Activity toActivity = find(fromActivity, Activity.class);
            for (org.omg.vdml.ResourceUse from : fromActivity.getResourceUse()) {
                ResourceUse to = findOrCreate(from, ResourceUse.class, toActivity);
                to.setName(from.getName());
                to.setDuration(measureBuilder.findOrCreateMeasure(from.getDuration()));
                to.setQuantity(measureBuilder.findOrCreateMeasure(from.getQuantity()));
                to.setResourceUseLocation(ResourceUseLocation.valueOf(from.getLocation().name()));
                to.setIsInputDriven(Boolean.TRUE.equals(from.getInputDriven()));
                if (from.getResource().size() == 1) {
                    org.omg.vdml.InputPort inputPort = from.getResource().get(0);
                    to.setInput(find(inputPort, InputPort.class));
                }
                org.omg.vdml.OutputPort outputPort = from.getDeliverable();
                if (outputPort != null) {
                    to.setOutput(find(outputPort, OutputPort.class));
                }
            }
        }
    }

    private void importDeliverableFlows(org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.DeliverableFlow from : c.getFlow()) {
            DeliverableFlow to = findOrCreate(from, DeliverableFlow.class, result);
            to.setName(from.getName());
            to.setSource(find(from.getProvider(), OutputPort.class));
            to.setTarget(find(from.getRecipient(), InputPort.class));
            org.omg.vdml.BusinessItem d = from.getDeliverable();
            if (d.getDefinition() == null) {
                to.setDeliverable(find(d, BusinessItemDefinition.class));
            } else {
                to.setDeliverable(find(d.getDefinition(), BusinessItemDefinition.class));
            }
            measureBuilder.fromMeasuredCharacteristics(to.getMeasures(), from.getMeasuredCharacteristic());
//            if (from.getProvider().getBatchSize() != null) {
//                to.setQuantity(measureBuilder.findOrCreateMeasure(from.getProvider().getBatchSize()));
//            } else if (from.getRecipient().getBatchSize() != null) {
//                to.setQuantity(measureBuilder.findOrCreateMeasure(from.getRecipient().getBatchSize()));
//            }
            if (from.getMilestone() != null) {
                to.setMilestone(find(from.getMilestone(), Milestone.class));
            }
        }
    }

    private void importPortDelegations(Collaboration result, EList<org.omg.vdml.PortDelegation> delegations) {
        for (org.omg.vdml.PortDelegation d : delegations) {
            if (d instanceof org.omg.vdml.InputDelegation) {
                org.omg.vdml.InputDelegation from = (org.omg.vdml.InputDelegation) d;
                InputDelegation to = findOrCreate(from, InputDelegation.class, result);
                to.setName(from.getName());
                to.setSource(find(from.getSource(), Port.class));
                to.setTarget(find(from.getTarget(), Port.class));
                org.omg.vdml.BusinessItemLibraryElement bid = from.getSource().getInputDefinition();
                if (bid == null) {
                    bid = from.getTarget().getInputDefinition();
                }
                if (bid != null) {
                    to.setDeliverable(find(bid, BusinessItemDefinition.class));
                }
            } else {
                org.omg.vdml.OutputDelegation from = (org.omg.vdml.OutputDelegation) d;
                OutputDelegation to = findOrCreate(from, OutputDelegation.class, result);
                to.setName(from.getName());
                to.setSource(find(from.getSource(), Port.class));
                to.setTarget(find(from.getTarget(), Port.class));
                org.omg.vdml.BusinessItemLibraryElement bid = from.getSource().getOutputDefinition();
                if (bid == null) {
                    bid = from.getTarget().getOutputDefinition();
                }
                if (bid != null) {
                    to.setDeliverable(find(bid, BusinessItemDefinition.class));
                }
            }
        }
    }

    private void importSupplyingStores(org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.SupplyingStore from : c.getSupplyingStore()) {
            SupplyingStore to = findOrCreate(from, SupplyingStore.class, findOrCreate(from.getSupplyingRole(), Role.class, result));
            to.setName(from.getName());
            to.setStoreRequirement(findOrCreate(from.getStoreRequirement(), StoreDefinition.class));
            measureBuilder.fromMeasuredCharacteristics(to.getMeasures(), from.getMeasuredCharacteristic());
            to.setStoreRequirement(findOrCreate(from.getStoreRequirement(), StoreDefinition.class));
            importPorts(from, to);
        }
    }

    private void importActivities(org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.Activity from : c.getActivity()) {
            Activity to = findOrCreate(from, Activity.class, findOrCreate(from.getPerformingRole(), Role.class, result));
            to.setName(from.getName());
            to.setCapabilityRequirement(findOrCreate(from.getCapabilityRequirement(), Capability.class));
            measureBuilder.fromMeasuredCharacteristics(to.getMeasures(), from.getMeasuredCharacteristic());
            to.setCapabilityRequirement(findOrCreate(from.getCapabilityRequirement(), Capability.class));
            importPorts(from, to);
        }
    }

    private void importPorts(org.omg.vdml.PortContainer fromPortContainer, PortContainer toPortContainer) {
        for (org.omg.vdml.Port from : fromPortContainer.getContainedPort()) {
            Port to = null;
            if (from instanceof org.omg.vdml.InputPort) {
                to = findOrCreate(from, InputPort.class, toPortContainer);
            } else {
                OutputPort op = findOrCreate(from, OutputPort.class, toPortContainer);
                for (org.omg.vdml.ValueAdd fromValueAdd : ((org.omg.vdml.OutputPort) from).getValueAdd()) {
                    ValueAdd toValueAdd = findOrCreate(fromValueAdd, ValueAdd.class, op);
                    toValueAdd.setName(fromValueAdd.getName());
                    measureBuilder.fromMeasuredCharacteristics(toValueAdd.getMeasures(), fromValueAdd.getMeasuredCharacteristic());
                    toValueAdd.setValueMeasure(measureBuilder.findOrCreateMeasure(fromValueAdd.getValueMeasurement()));
                }
                to = op;
            }
            to.setName(from.getName());
            measureBuilder.fromMeasuredCharacteristics(to.getMeasures(), from.getMeasuredCharacteristic());
            to.setBatchSize(measureBuilder.findOrCreateMeasure(from.getBatchSize()));
        }
    }

    private void importRoles(org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.Role from : c.getCollaborationRole()) {
            Role to = findOrCreate(from, from instanceof org.omg.vdml.Performer ? RoleInCapabilityMethod.class : RoleInNetwork.class, result);
            to.setName(from.getName());
        }
    }

    private void importBusinessItems(org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.BusinessItem from : c.getBusinessItem()) {
            BusinessItemDefinition to = null;
            if (from.getDefinition() != null) {
                to = findOrCreate(from.getDefinition(), BusinessItemDefinition.class, result);
                to.setName(from.getDefinition().getName());
                to.setFungible(from.getDefinition().getIsFungible());
                to.setShareable(from.getDefinition().getIsShareable());
            } else {
                to = findOrCreate(from, BusinessItemDefinition.class, result);
                to.setName(from.getName());
                to.setFungible(from.getIsFungible());
                to.setShareable(from.getIsShareable());
            }
            if (!result.getBusinessItemDefinitions().contains(to)) {
                result.getBusinessItemDefinitions().add(to);
                to.getCollaborations().add(result);
            }
            measureBuilder.fromMeasuredCharacteristics(to.getMeasures(), from.getMeasuredCharacteristic());
        }
    }

    private <T extends MetaEntity> T find(EObject eObject, Class<T> rt) {
        if (eObject == null) {
            return null;
        }
        return entityManager.find(rt, buildUri(eObject));
    }

    public BusinessItemDefinition findBusinessItemDefinition(String s) {
        return entityManager.find(BusinessItemDefinition.class, s);
    }

    public StoreDefinition findStoreDefinition(String s) {
        return entityManager.find(StoreDefinition.class, s);
    }

    public Capability findCapabilityDefinition(String s) {
        return entityManager.find(Capability.class, s);
    }

    public Collaboration findCollaboration(String s) {
        return entityManager.find(Collaboration.class, s);
    }

    public Role findRole(String s) {
        return entityManager.find(Role.class, s);
    }
}
