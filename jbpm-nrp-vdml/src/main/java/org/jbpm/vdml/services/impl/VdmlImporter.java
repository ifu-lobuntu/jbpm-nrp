package org.jbpm.vdml.services.impl;


import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.meta.Activity;
import org.jbpm.vdml.services.impl.model.meta.Capability;
import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.meta.DeliverableFlow;
import org.jbpm.vdml.services.impl.model.meta.Milestone;
import org.jbpm.vdml.services.impl.model.meta.OutputDelegation;
import org.jbpm.vdml.services.impl.model.meta.PoolDefinition;
import org.jbpm.vdml.services.impl.model.meta.PortContainer;
import org.jbpm.vdml.services.impl.model.meta.ResourceUseLocation;
import org.jbpm.vdml.services.impl.model.meta.Role;
import org.jbpm.vdml.services.impl.model.meta.StoreDefinition;
import org.jbpm.vdml.services.impl.model.meta.SupplyingStore;
import org.jbpm.vdml.services.impl.model.meta.ResourceUse;
import org.jbpm.vdml.services.impl.model.meta.BusinessItemDefinition;
import org.jbpm.vdml.services.impl.model.meta.ValueProposition;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;
import org.jbpm.vdml.services.impl.model.meta.InputDelegation;
import org.jbpm.vdml.services.impl.model.runtime.ExchangeConfiguration;
import org.omg.vdml.*;


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

        importCapabilities(vdm);
        importStoreDefinitions(vdm);
        importBusinessItemDefinitions(vdm);
        for (org.omg.vdml.Collaboration collaboration : vdm.getCollaboration()) {
            buildCollaboration(deploymentId, collaboration);
        }
        configureCapabilities(vdm);
        configureStoreDefinitions(vdm);
        entityManager.flush();
    }

    private void configureStoreDefinitions(ValueDeliveryModel vdm) {
        for (StoreLibrary library : vdm.getStoreLibrary()) {
            for (org.omg.vdml.StoreDefinition from : library.getStoreDefinitions()) {
                StoreDefinition to = find(from, StoreDefinition.class);
                org.omg.vdml.ExchangeConfiguration fromEc = from.getExchangeConfiguration();
                to.setExchangeConfiguration(buildExchangeConfiguration(fromEc));
            }
        }
    }

    private ExchangeConfiguration buildExchangeConfiguration(org.omg.vdml.ExchangeConfiguration fromEc) {
        ExchangeConfiguration ec = null;
        if (fromEc != null) {
            ec = new ExchangeConfiguration();
            ec.setCollaborationToUse(find(fromEc.getExchangeMethod(), Collaboration.class));
            ec.setSupplierRole(find(fromEc.getSupplierRole(), Role.class));
            ec.setExchangeMilestone(find(fromEc.getExchangeMilestone(), Milestone.class));
            ec.setPoolBooking(find(fromEc.getResourceUseFromPool(),ResourceUse.class));
            entityManager.persist(ec);
            entityManager.flush();
        }
        return ec;
    }

    private void configureCapabilities(ValueDeliveryModel vdm) {
        for (CapabilityLibrary l : vdm.getCapabilitylibrary()) {
            for (org.omg.vdml.Capability from : l.getCapability()) {
                Capability to = find(from, Capability.class);
                org.omg.vdml.ExchangeConfiguration fromEc = from.getExchangeConfiguration();
                to.setExchangeConfiguration(buildExchangeConfiguration(fromEc));
            }
        }
    }

    private void importStoreDefinitions(ValueDeliveryModel vdm) {
        for (StoreLibrary library : vdm.getStoreLibrary()) {
            for (org.omg.vdml.StoreDefinition from : library.getStoreDefinitions()) {
                StoreDefinition to = findOrCreate(from, from instanceof org.omg.vdml.PoolDefinition ? PoolDefinition.class : StoreDefinition.class);
                to.setName(from.getName());
                measureBuilder.fromCharacteristics(to.getMeasures(), from.getCharacteristicDefinition());
            }
        }
    }

    private void importCapabilities(ValueDeliveryModel vdm) {
        for (CapabilityLibrary l : vdm.getCapabilitylibrary()) {
            for (org.omg.vdml.Capability from : l.getCapability()) {
                Capability to = findOrCreate(from, Capability.class);
                to.setName(from.getName());
                measureBuilder.fromCharacteristics(to.getMeasures(), from.getCharacteristicDefinition());
            }
        }
    }

    private void importBusinessItemDefinitions(ValueDeliveryModel vdm) {
        for (BusinessItemLibrary library : vdm.getBusinessItemLibrary()) {
            for (BusinessItemLibraryElement from : library.getBusinessItemLibraryElement()) {
                BusinessItemDefinition to = findOrCreate(from, BusinessItemDefinition.class);
                to.setName(from.getName());
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
        Collaboration result = findOrCreate(c, Collaboration.class);
        result.setName(c.getName());
        result.setDeploymentId(deploymentId);
        importBusinessItems(c, result);
        importRoles(c, result);
        importActivities(c, result);
        importSupplyingStores(c, result);
        if (c instanceof CapabilityMethod) {
            EList<org.omg.vdml.Milestone> milestone = ((CapabilityMethod) c).getMilestone();
            for (org.omg.vdml.Milestone from : milestone) {
                Milestone to = findOrCreate(from, Milestone.class, result);
                to.setName(from.getName());
            }
        }

        importDeliverableFlows(c, result);
        importValuePropositions(c);
        importPortDelegations(result, c.getInternalPortDelegation());
        importContextBasedPortDelegations(deploymentId, c, result);
        importResourceUses(c);
        setInitiatingRole(result);
        entityManager.flush();
        return result;
    }

    protected void setInitiatingRole(Collaboration result) {
        List<Activity> initiatingActivity = new ArrayList<Activity>();
        for (Activity activity : result.getActivities()) {
            if (activity.getInputDeliverableFlows().isEmpty() && !activity.getOutputDeliverableFlows().isEmpty()) {
                initiatingActivity.add(activity);
            }
        }
        if (initiatingActivity.size() == 1) {
            result.setInitiatorRole(initiatingActivity.get(0).getPerformingRole());
        }
    }

    private void importContextBasedPortDelegations(String deploymentId, org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.Activity activity : c.getActivity()) {
            for (DelegationContext delegationContext : activity.getDelegationContext()) {
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
                    ValuePropositionComponent toVpc = findOrCreate(fromVp, ValuePropositionComponent.class, toVp);
                    toVpc.setName(fromVpc.getName());
                    measureBuilder.fromMeasuredCharacteristics(toVpc.getMeasures(), fromVpc.getMeasuredCharacteristic());
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
                if (from.getResource().size() == 1) {
                    InputPort inputPort = from.getResource().get(0);
                    if (inputPort.getInput() == null) {
                        if (inputPort.getDelegatedInput().size() == 1) {
                            to.setInput(find(inputPort.getDelegatedInput().get(0), InputDelegation.class));
                        }
                    } else {
                        to.setInput(find(inputPort.getInput(), DeliverableFlow.class));
                    }
                }
                OutputPort outputPort = from.getDeliverable();
                if (outputPort != null) {
                    if (outputPort.getOutput() == null) {
                        if (outputPort.getOutputDelegation().size() == 1) {
                            to.setInput(find(outputPort.getOutputDelegation().get(0), OutputDelegation.class));
                        }
                    } else {
                        to.setOutput(find(outputPort.getOutput(), DeliverableFlow.class));
                    }
                }
            }
        }
    }

    private void importDeliverableFlows(org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.DeliverableFlow from : c.getFlow()) {
            DeliverableFlow to = findOrCreate(from, DeliverableFlow.class, result);
            to.setName(from.getName());
            to.setTargetName(from.getRecipient().getName());
            to.setSourceName(from.getProvider().getName());
            to.setSourcePortContainer(find(from.getProvider().eContainer(), PortContainer.class));
            to.setTargetPortContainer(find(from.getRecipient().eContainer(), PortContainer.class));
            BusinessItem d = from.getDeliverable();
            if (d.getDefinition() == null) {
                to.setDeliverable(find(d, BusinessItemDefinition.class));
            } else {
                to.setDeliverable(find(d.getDefinition(), BusinessItemDefinition.class));
            }
            addValueAdds(to, from.getProvider().getValueAdd());
            measureBuilder.fromMeasuredCharacteristics(to.getMeasures(), from.getMeasuredCharacteristic());
            if (from.getProvider().getBatchSize() != null) {
                to.setQuantity(measureBuilder.findOrCreateMeasure(from.getProvider().getBatchSize()));
            } else if (from.getRecipient().getBatchSize() != null) {
                to.setQuantity(measureBuilder.findOrCreateMeasure(from.getRecipient().getBatchSize()));
            }
            if (from.getMilestone() != null) {
                to.setMilestone(find(from.getMilestone(), Milestone.class));
            }
        }
    }

    private void addValueAdds(DirectedFlow to, EList<ValueAdd> valueAdd) {
        for (ValueAdd fromValueAdd : valueAdd) {
            Measure toValueAdd = measureBuilder.findOrCreateMeasure(fromValueAdd.getValueMeasurement());
            if (toValueAdd != null) {
                to.getValueAdds().add(toValueAdd);
            }
        }
    }

    private void importPortDelegations(Collaboration result, EList<org.omg.vdml.PortDelegation> delegations) {
        for (org.omg.vdml.PortDelegation d : delegations) {
            if (d instanceof org.omg.vdml.InputDelegation) {
                org.omg.vdml.InputDelegation from = (org.omg.vdml.InputDelegation) d;
                InputDelegation to = findOrCreate(from, InputDelegation.class, result);
                to.setName(from.getName());
                to.setTargetName(from.getSource().getName());
                to.setSourceName(from.getTarget().getName());
                to.setSourcePortContainer(find(from.getSource().eContainer(), PortContainer.class));
                to.setTargetPortContainer(find(from.getTarget().eContainer(), PortContainer.class));
                BusinessItemLibraryElement bid = from.getSource().getInputDefinition();
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
                to.setTargetName(from.getSource().getName());
                to.setSourceName(from.getTarget().getName());
                to.setSourcePortContainer(find(from.getSource().eContainer(), PortContainer.class));
                to.setTargetPortContainer(find(from.getTarget().eContainer(), PortContainer.class));
                BusinessItemLibraryElement bid = from.getSource().getOutputDefinition();
                if (bid == null) {
                    bid = from.getTarget().getOutputDefinition();
                }
                if (bid != null) {
                    to.setDeliverable(find(bid, BusinessItemDefinition.class));
                }
                addValueAdds(to, from.getSource().getValueAdd());
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
        }
    }

    private void importActivities(org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.Activity from : c.getActivity()) {
            Activity to = findOrCreate(from, Activity.class, findOrCreate(from.getPerformingRole(), Role.class, result));
            to.setName(from.getName());
            to.setCapabilityRequirement(findOrCreate(from.getCapabilityRequirement(), Capability.class));
            measureBuilder.fromMeasuredCharacteristics(to.getMeasures(), from.getMeasuredCharacteristic());
            to.setCapabilityRequirement(findOrCreate(from.getCapabilityRequirement(), Capability.class));
        }
    }

    private void importRoles(org.omg.vdml.Collaboration c, Collaboration result) {
        for (org.omg.vdml.Role from : c.getCollaborationRole()) {
            Role to = findOrCreate(from, Role.class, result);
            to.setName(from.getName());
        }
    }

    private void importBusinessItems(org.omg.vdml.Collaboration c, Collaboration result) {
        for (BusinessItem from : c.getBusinessItem()) {
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
        if(eObject==null){
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
