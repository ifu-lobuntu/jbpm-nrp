package org.jbpm.vdml.services;

import org.jbpm.vdml.services.impl.MetaBuilder;
import org.jbpm.vdml.services.impl.ParticipantService;
import org.jbpm.vdml.services.impl.VdmlImporter;
import org.jbpm.vdml.services.impl.model.runtime.IndividualParticipant;
import org.junit.Test;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;
import org.omg.vdml.Activity;
import org.omg.vdml.BusinessItemDefinition;
import org.omg.vdml.InputDelegation;
import org.omg.vdml.OutputDelegation;
import org.omg.vdml.Role;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

public class ValueCalculateTest extends MetaEntityImportTest {
    @Test
    public void testIt() throws Exception{
        ValueDeliveryModel vdm = buildModel();

        BusinessItemDefinition userStory = VDMLFactory.eINSTANCE.createBusinessItemDefinition();
        userStory.setName("UserStory");
        vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement().add(userStory);

        BusinessItemDefinition usableFeature = VDMLFactory.eINSTANCE.createBusinessItemDefinition();
        usableFeature.setName("UsableFeature");
        vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement().add(usableFeature);

        CapabilityMethod implementUserStoryMethod = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(implementUserStoryMethod);
        implementUserStoryMethod.setName("ImplementUserStory");

        InputPort userStoryIntoMethod = VDMLFactory.eINSTANCE.createInputPort();
        userStoryIntoMethod.setName("userStoryIntoMethod");
        implementUserStoryMethod.getContainedPort().add(userStoryIntoMethod);
        userStoryIntoMethod.setInputDefinition(userStory);

        OutputPort usableFeatureOutOfMethod = VDMLFactory.eINSTANCE.createOutputPort();
        usableFeatureOutOfMethod.setName("usableFeatureOutOfMethod");
        implementUserStoryMethod.getContainedPort().add(usableFeatureOutOfMethod);
        usableFeatureOutOfMethod.setOutputDefinition(usableFeature);


        Role developer = VDMLFactory.eINSTANCE.createPerformer();
        implementUserStoryMethod.getCollaborationRole().add(developer);
        developer.setName("Developer");

        Activity writeCodeActivity = VDMLFactory.eINSTANCE.createActivity();
        writeCodeActivity.setName("WriteTest");
        implementUserStoryMethod.getActivity().add(writeCodeActivity);
        writeCodeActivity.setPerformingRole(developer);
        InputPort userStoryIntoWriteTest=VDMLFactory.eINSTANCE.createInputPort();
        writeCodeActivity.getContainedPort().add(userStoryIntoWriteTest);
        userStoryIntoWriteTest.setName("userStoryIntoWriteTest");
        userStoryIntoWriteTest.setInputDefinition(userStory);
        CapabilityDefinition capability=VDMLFactory.eINSTANCE.createCapabilityDefinition();
        capability.setName("WritingTests");
        Characteristic coverage = buildDirectMeasure(vdm);
        coverage.getMeasure().get(0).setName("CoverageDelta");
        capability.getCharacteristicDefinition().add(coverage);
        vdm.getCapabilitylibrary().get(0).getCapability().add(capability);
        writeCodeActivity.setCapabilityRequirement(capability);

        OutputPort useableFeatureoutOfWriteTest=VDMLFactory.eINSTANCE.createOutputPort();
        writeCodeActivity.getContainedPort().add(useableFeatureoutOfWriteTest);
        useableFeatureoutOfWriteTest.setName("useableFeatureoutOfWriteTest");
        useableFeatureoutOfWriteTest.setOutputDefinition(usableFeature);

        BusinessItem usableFeatureBusinessItem = VDMLFactory.eINSTANCE.createBusinessItem();
        usableFeatureBusinessItem.setName("UsableFeature");
        usableFeatureBusinessItem.setDefinition(usableFeature);
        implementUserStoryMethod.getBusinessItem().add(usableFeatureBusinessItem);

        BusinessItem userStoryBusinessItem = VDMLFactory.eINSTANCE.createBusinessItem();
        userStoryBusinessItem.setName("UserStory");
        userStoryBusinessItem.setDefinition(userStory);
        implementUserStoryMethod.getBusinessItem().add(userStoryBusinessItem);

        InputDelegation internalUserStoryDelegation=VDMLFactory.eINSTANCE.createInputDelegation();
        implementUserStoryMethod.getInternalPortDelegation().add(internalUserStoryDelegation);
        internalUserStoryDelegation.setName("internalUserStoryDelegation");
        internalUserStoryDelegation.setSource(userStoryIntoMethod);
        internalUserStoryDelegation.setTarget(userStoryIntoWriteTest);

        OutputDelegation internalUsableFeatureDelegation=VDMLFactory.eINSTANCE.createOutputDelegation();
        implementUserStoryMethod.getInternalPortDelegation().add(internalUsableFeatureDelegation);
        internalUsableFeatureDelegation.setName("internalUsableFeatureDelegation");
        internalUsableFeatureDelegation.setSource(useableFeatureoutOfWriteTest);
        internalUsableFeatureDelegation.setTarget(usableFeatureOutOfMethod);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildCollaboration(DEFAULT_DEPLOYMENT_ID, implementUserStoryMethod);
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration=new VdmlImporter(getEntityManager()).findCollaboration(MetaBuilder.buildUri(implementUserStoryMethod));
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant ekke = participantService.createIndividualParticipant("ekke");
        participantService.setCapabilities(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(capability)));
        //WHEN



    }
}
