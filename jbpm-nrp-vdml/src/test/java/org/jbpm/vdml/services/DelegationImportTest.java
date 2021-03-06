package org.jbpm.vdml.services;


import org.jbpm.vdml.services.impl.MetaBuilder;
import org.jbpm.vdml.services.impl.VdmlImporter;
import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.junit.Test;
import org.omg.vdml.*;
import org.omg.vdml.Activity;
import org.omg.vdml.BusinessItemDefinition;
import org.omg.vdml.CapabilityMethod;
import org.omg.vdml.InputDelegation;
import org.omg.vdml.InputPort;
import org.omg.vdml.OutputDelegation;
import org.omg.vdml.OutputPort;
import org.omg.vdml.Role;
import org.omg.vdml.ValueAdd;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class DelegationImportTest extends MetaEntityImportTest {


    @Test
    public void testInternalDelegation() throws Exception {
        ValueDeliveryModel vdm = buildModel();

        BusinessItemDefinition bid =VDMLFactory.eINSTANCE.createBusinessItemDefinition();
        bid.setName("StuffDef");
        vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement().add(bid);

        CapabilityMethod cp = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cp);
        cp.setName("MyCapabilityMethod");
        InputPort capabilityMethodInput = VDMLFactory.eINSTANCE.createInputPort();
        capabilityMethodInput.setName("capabilityMethodInput");
        cp.getContainedPort().add(capabilityMethodInput);
        capabilityMethodInput.setInputDefinition(bid);

        OutputPort capabilityMethodOutput = VDMLFactory.eINSTANCE.createOutputPort();
        capabilityMethodOutput.setName("capabilityMethodOutput");
        cp.getContainedPort().add(capabilityMethodOutput);
        capabilityMethodOutput.setOutputDefinition(bid);


        Role role = VDMLFactory.eINSTANCE.createPerformer();
        cp.getCollaborationRole().add(role);
        role.setName("MyRole");

        Activity activity = VDMLFactory.eINSTANCE.createActivity();
        activity.setName("DoStuff");
        cp.getActivity().add(activity);
        activity.setPerformingRole(role);
        InputPort activityInput=VDMLFactory.eINSTANCE.createInputPort();
        activity.getContainedPort().add(activityInput);
        activityInput.setName("activityInput");
        activityInput.setInputDefinition(bid);
        OutputPort activityOutput=VDMLFactory.eINSTANCE.createOutputPort();
        activity.getContainedPort().add(activityOutput);
        activityOutput.setName("activityOutput");
        activityOutput.setOutputDefinition(bid);
        ValueAdd activityValueAdd = VDMLFactory.eINSTANCE.createValueAdd();
        activityOutput.getValueAdd().add(activityValueAdd);
        activityValueAdd.setName("activityValueAdd");
        activityValueAdd.setValueMeasurement(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        activityValueAdd.getValueMeasurement().setCharacteristicDefinition(buildDirectMeasure(vdm));

        BusinessItem bi = VDMLFactory.eINSTANCE.createBusinessItem();
        bi.setName("Stuff");
        bi.setDefinition(bid);
        cp.getBusinessItem().add(bi);

        InputDelegation capabilityMethodInputToActivityInput=VDMLFactory.eINSTANCE.createInputDelegation();
        cp.getInternalPortDelegation().add(capabilityMethodInputToActivityInput);
        capabilityMethodInputToActivityInput.setName("capabilityMethodInputToActivityInput");
        capabilityMethodInputToActivityInput.setSource(capabilityMethodInput);
        capabilityMethodInputToActivityInput.setTarget(activityInput);

        OutputDelegation activityOutputToCapabilityMethodOutput=VDMLFactory.eINSTANCE.createOutputDelegation();
        cp.getInternalPortDelegation().add(activityOutputToCapabilityMethodOutput);
        activityOutputToCapabilityMethodOutput.setName("activityOutputToCapabilityMethodOutput");
        activityOutputToCapabilityMethodOutput.setSource(activityOutput);
        activityOutputToCapabilityMethodOutput.setTarget(capabilityMethodOutput);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        //WHEN
        new VdmlImporter(getEntityManager()).buildCollaboration(DEFAULT_DEPLOYMENT_ID, cp);
        org.jbpm.vdml.services.impl.model.meta.CapabilityMethod collaboration=(org.jbpm.vdml.services.impl.model.meta.CapabilityMethod) new VdmlImporter(getEntityManager()).findCollaboration(MetaBuilder.buildUri(cp));

        //THEN
        assertEquals(cp.getName(), collaboration.getName());
        assertEquals(1, collaboration.getCollaborationRoles().size());
        assertEquals("MyRole", collaboration.getCollaborationRoles().iterator().next().getName());
        assertSame(collaboration, collaboration.getCollaborationRoles().iterator().next().getCollaboration());

        assertEquals(1, collaboration.getActivities().size());
        assertEquals("DoStuff", collaboration.getActivities().iterator().next().getName());
        assertSame(collaboration, collaboration.getActivities().iterator().next().getCollaboration());
        assertEquals("MyRole", collaboration.getActivities().iterator().next().getPerformingRole().getName());
        assertSame(collaboration.getCollaborationRoles().iterator().next(), collaboration.getActivities().iterator().next().getPerformingRole());

        assertEquals(2, collaboration.getFlows().size());
        assertEquals(1, collaboration.getInput().size());
        assertEquals(1, collaboration.getOutput().size());
        assertEquals(1, collaboration.getInput().iterator().next().getInputDelegations().size());
        assertEquals(1, collaboration.getOutput().iterator().next().getDelegatedOutputs().size());
        assertEquals(0, collaboration.getOutput().iterator().next().getValueAdds().size());
        assertEquals(1, collaboration.getActivities().iterator().next().getInput().size());
        assertEquals(1, collaboration.getActivities().iterator().next().getInput().iterator().next().getDelegatedInputs().size());
        assertEquals(1, collaboration.getActivities().iterator().next().getOutput().iterator().next().getValueAdds().size());
        assertEquals(1, collaboration.getActivities().iterator().next().getOutput().iterator().next().getOutputDelegations().size());
        assertSame(collaboration.getInput().iterator().next().getInputDelegations().iterator().next(),collaboration.getActivities().iterator().next().getInput().iterator().next().getDelegatedInputs().iterator().next());
        assertSame(collaboration.getOutput().iterator().next().getDelegatedOutputs().iterator().next(), collaboration.getActivities().iterator().next().getOutput().iterator().next().getOutputDelegations().iterator().next());
    }
    @Test
    public void testDelegationContext() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        CapabilityMethod cm=VDMLFactory.eINSTANCE.createCapabilityMethod();
        cm.setName("RootCapabilityMethod");
        vdm.getCollaboration().add(cm);
        BusinessItemDefinition bid =VDMLFactory.eINSTANCE.createBusinessItemDefinition();
        bid.setName("StuffDef");
        vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement().add(bid);

        Role role = VDMLFactory.eINSTANCE.createPerformer();
        cm.getCollaborationRole().add(role);
        role.setName("MyRole");

        Activity activity = VDMLFactory.eINSTANCE.createActivity();
        activity.setName("DoStuff");
        cm.getActivity().add(activity);
        activity.setPerformingRole(role);
        InputPort activityInput=VDMLFactory.eINSTANCE.createInputPort();
        activity.getContainedPort().add(activityInput);
        activityInput.setName("activityInput");
        activityInput.setInputDefinition(bid);

        OutputPort activityOutput=VDMLFactory.eINSTANCE.createOutputPort();
        activity.getContainedPort().add(activityOutput);
        activityOutput.setName("activityOutput");
        activityOutput.setOutputDefinition(bid);

        BusinessItem bi = VDMLFactory.eINSTANCE.createBusinessItem();
        bi.setName("Stuff");
        bi.setDefinition(bid);
        cm.getBusinessItem().add(bi);

        CapabilityMethod delegatedCapabilityMethod = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(delegatedCapabilityMethod);
        delegatedCapabilityMethod.setName("MyCapabilityMethod");
        InputPort capabilityMethodInput = VDMLFactory.eINSTANCE.createInputPort();
        capabilityMethodInput.setName("capabilityMethodInput");
        delegatedCapabilityMethod.getContainedPort().add(capabilityMethodInput);
        capabilityMethodInput.setInputDefinition(bid);

        DelegationContext dc = VDMLFactory.eINSTANCE.createDelegationContext();
        dc.setContextCollaboration(delegatedCapabilityMethod);
        dc.setDelegatedActivity(activity);
        vdm.getScenario().get(0).getDelegationtContext().add(dc);

        OutputPort capabilityMethodOutput = VDMLFactory.eINSTANCE.createOutputPort();
        capabilityMethodOutput.setName("capabilityMethodOutput");
        delegatedCapabilityMethod.getContainedPort().add(capabilityMethodOutput);
        capabilityMethodOutput.setOutputDefinition(bid);

        ValueAdd capabilityMethodValueAdd = VDMLFactory.eINSTANCE.createValueAdd();
        capabilityMethodOutput.getValueAdd().add(capabilityMethodValueAdd);
        capabilityMethodValueAdd.setName("capabilityMethodValueAdd");
        capabilityMethodValueAdd.setValueMeasurement(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        capabilityMethodValueAdd.getValueMeasurement().setCharacteristicDefinition(buildDirectMeasure(vdm));

        InputDelegation activityInputToCapabilityMethodInput=VDMLFactory.eINSTANCE.createInputDelegation();
        dc.getContextBasedPortDelegation().add(activityInputToCapabilityMethodInput);
        activityInputToCapabilityMethodInput.setName("activityInputToCapabilityMethodInput");
        activityInputToCapabilityMethodInput.setSource(activityInput);
        activityInputToCapabilityMethodInput.setTarget(capabilityMethodInput);

        OutputDelegation capabilityMethodOutputToActivityOutput=VDMLFactory.eINSTANCE.createOutputDelegation();
        dc.getContextBasedPortDelegation().add(capabilityMethodOutputToActivityOutput);
        capabilityMethodOutputToActivityOutput.setName("capabilityMethodOutputToActivityOutput");
        capabilityMethodOutputToActivityOutput.setSource(capabilityMethodOutput);
        capabilityMethodOutputToActivityOutput.setTarget(activityOutput);


        vdm.eResource().save(new ByteArrayOutputStream(), null);
        //WHEN
        new VdmlImporter(getEntityManager()).buildCollaboration(DEFAULT_DEPLOYMENT_ID, delegatedCapabilityMethod);

        org.jbpm.vdml.services.impl.model.meta.CapabilityMethod owningCollaboration = (org.jbpm.vdml.services.impl.model.meta.CapabilityMethod) new VdmlImporter(getEntityManager()).buildCollaboration(DEFAULT_DEPLOYMENT_ID, cm);
        //THEN
        assertEquals(cm.getName(), owningCollaboration.getName());
        assertEquals(1, owningCollaboration.getCollaborationRoles().size());
        assertEquals("MyRole", owningCollaboration.getCollaborationRoles().iterator().next().getName());
        assertSame(owningCollaboration, owningCollaboration.getCollaborationRoles().iterator().next().getCollaboration());

        assertEquals(1, owningCollaboration.getActivities().size());
        assertEquals("DoStuff", owningCollaboration.getActivities().iterator().next().getName());
        assertSame(owningCollaboration, owningCollaboration.getActivities().iterator().next().getCollaboration());
        assertEquals("MyRole", owningCollaboration.getActivities().iterator().next().getPerformingRole().getName());
        assertSame(owningCollaboration.getCollaborationRoles().iterator().next(), owningCollaboration.getActivities().iterator().next().getPerformingRole());

        assertEquals(2, owningCollaboration.getFlows().size());
        Collaboration collaboration = new VdmlImporter(getEntityManager()).findCollaboration(MetaBuilder.buildUri(delegatedCapabilityMethod));
        assertEquals(1, collaboration.getOutput().size());
        assertEquals(1, collaboration.getInput().size());
        assertEquals(1, collaboration.getOutput().iterator().next().getOutputDelegations().size());
        assertEquals(1, collaboration.getInput().iterator().next().getDelegatedInputs().size());
        assertEquals(1, collaboration.getOutput().iterator().next().getValueAdds().size());
        assertEquals(1, owningCollaboration.getActivities().iterator().next().getOutput().size());
        assertEquals(1, owningCollaboration.getActivities().iterator().next().getOutput().iterator().next().getDelegatedOutputs().size());
        assertEquals(0, owningCollaboration.getActivities().iterator().next().getOutput().iterator().next().getValueAdds().size());
        assertEquals(1, owningCollaboration.getActivities().iterator().next().getInput().size());
        assertEquals(1, owningCollaboration.getActivities().iterator().next().getInput().iterator().next().getInputDelegations().size());
        assertEquals(collaboration.getInput().iterator().next().getDelegatedInputs().iterator().next().getUri(), owningCollaboration.getActivities().iterator().next().getInput().iterator().next().getInputDelegations().iterator().next().getUri());
        assertEquals(collaboration.getOutput().iterator().next().getOutputDelegations().iterator().next().getUri(), owningCollaboration.getActivities().iterator().next().getOutput().iterator().next().getDelegatedOutputs().iterator().next().getUri());

    }
}
