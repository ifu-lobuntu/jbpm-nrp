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
import org.omg.vdml.DeliverableFlow;
import org.omg.vdml.InputPort;
import org.omg.vdml.OutputPort;
import org.omg.vdml.Role;
import org.omg.vdml.ValueAdd;
import org.omg.vdml.ValueProposition;
import org.omg.vdml.ValuePropositionComponent;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class PropositionExchangeImportTest extends MetaEntityImportTest {

    @Test
    public void testPropositionExchange() throws Exception {
        //Given
        ValueDeliveryModel vdm = buildModel();


        CapabilityMethod cp = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cp);
        cp.setName("MyCapabilityMethod");

        Role myRole = VDMLFactory.eINSTANCE.createPerformer();
        cp.getCollaborationRole().add(myRole);
        myRole.setName("MyRole");

        Role yourRole = VDMLFactory.eINSTANCE.createPerformer();
        cp.getCollaborationRole().add(yourRole);
        yourRole.setName("YourRole");

        ValueProposition vp = VDMLFactory.eINSTANCE.createValueProposition();
        vp.setName("FromMeToYou");
        myRole.getProvidedProposition().add(vp);
        vp.setRecipient(yourRole);

        ValuePropositionComponent vpc = VDMLFactory.eINSTANCE.createValuePropositionComponent();
        vpc.setName("ValuePropositionComponent");
        vp.getComponent().add(vpc);


        Activity myActivity = VDMLFactory.eINSTANCE.createActivity();
        myActivity.setName("DoMyStuff");
        cp.getActivity().add(myActivity);
        myActivity.setPerformingRole(myRole);

        OutputPort myOutput=VDMLFactory.eINSTANCE.createOutputPort();
        myOutput.setName("myOutput");
        myActivity.getContainedPort().add(myOutput);
        myOutput.setBatchSize(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        myOutput.getBatchSize().setCharacteristicDefinition(buildDirectMeasure(vdm));

        ValueAdd myValueAdd = VDMLFactory.eINSTANCE.createValueAdd();
        myValueAdd.setName("myValueAdd");
        myValueAdd.setValueMeasurement(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        myValueAdd.getValueMeasurement().setCharacteristicDefinition(buildDirectMeasure(vdm));
        myOutput.getValueAdd().add(myValueAdd);
        vpc.getAggregatedFrom().add(myValueAdd);
        MeasuredCharacteristic measuredCharacteristic = VDMLFactory.eINSTANCE.createMeasuredCharacteristic();
        vpc.getMeasuredCharacteristic().add(measuredCharacteristic);
        measuredCharacteristic.setCharacteristicDefinition(buildCollectiveMeasure(vdm, myValueAdd.getValueMeasurement().getCharacteristicDefinition(), "CollectiveMeasure"));



        Activity yourActivity = VDMLFactory.eINSTANCE.createActivity();
        yourActivity.setName("DoYourStuff");
        cp.getActivity().add(yourActivity);
        yourActivity.setPerformingRole(yourRole);


        InputPort yourInput=VDMLFactory.eINSTANCE.createInputPort();
        yourInput.setName("yourInput");
        yourActivity.getContainedPort().add(yourInput);

        DeliverableFlow flow=VDMLFactory.eINSTANCE.createDeliverableFlow();
        flow.setName("flow");
        cp.getFlow().add(flow);
        flow.setRecipient(yourInput);
        flow.setProvider(myOutput);
        flow.setDuration(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());

        BusinessItemDefinition bid =VDMLFactory.eINSTANCE.createBusinessItemDefinition();
        bid.setName("StuffDef");
        vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement().add(bid);

        BusinessItem bi = VDMLFactory.eINSTANCE.createBusinessItem();
        bi.setName("Stuff");
        bi.setDefinition(bid);
        flow.setDeliverable(bi);
        cp.getBusinessItem().add(bi);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        //When
        VdmlImporter importer = new VdmlImporter(getEntityManager());
        importer.buildCollaboration(DEFAULT_DEPLOYMENT_ID, cp);
        org.jbpm.vdml.services.impl.model.meta.CapabilityMethod collaboration= (org.jbpm.vdml.services.impl.model.meta.CapabilityMethod) importer.findCollaboration(MetaBuilder.buildUri(cp));

        //Then
        //THEN
        assertEquals(cp.getName(), collaboration.getName());
        assertEquals(2, collaboration.getCollaborationRoles().size());

        assertEquals(2, collaboration.getActivities().size());


        assertEquals(1, collaboration.getFlows().size());
        assertEquals("flow", collaboration.getFlows().iterator().next().getName());
        assertEquals(1, ((org.jbpm.vdml.services.impl.model.meta.OutputPort)collaboration.getFlows().iterator().next().getSource()).getValueAdds().size());
        org.jbpm.vdml.services.impl.model.meta.Role myFoundRole=importer.findRole(MetaBuilder.buildUri(myRole));
        assertEquals(1, myFoundRole.getProvidedValuePropositions().size());
        assertEquals(1, myFoundRole.getProvidedValuePropositions().iterator().next().getComponents().size());
        assertEquals(1, myFoundRole.getProvidedValuePropositions().iterator().next().getComponents().iterator().next().getMeasures().size());
        Measure m = myFoundRole.getProvidedValuePropositions().iterator().next().getComponents().iterator().next().getMeasures().iterator().next();
        CollectiveMeasure cm= (CollectiveMeasure) m;
        org.jbpm.vdml.services.impl.model.meta.ValueAdd m2 = ((org.jbpm.vdml.services.impl.model.meta.OutputPort)collaboration.getFlows().iterator().next().getSource()).getValueAdds().iterator().next();
        DirectMeasure dm= (DirectMeasure) m2.getValueMeasure();
        assertEquals(dm.getUri(), cm.getAggregatedMeasures().iterator().next().getUri());
    }
}
