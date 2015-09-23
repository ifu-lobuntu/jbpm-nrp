package org.jbpm.vdml.services;

import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.junit.Test;
import org.omg.smm.BinaryFunctor;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;
import org.omg.vdml.Activity;
import org.omg.vdml.BusinessItemDefinition;
import org.omg.vdml.InputDelegation;
import org.omg.vdml.OutputDelegation;
import org.omg.vdml.Role;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ValueCalculateTest extends MetaEntityImportTest {
    @Test
    public void testBusinessItem() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        //GIVEN
        BusinessItemDefinition userStory = super.createBusinessItemDefinition(vdm, "UserStory");
        Characteristic userStoryEffort = super.buildDirectMeasure(vdm, "UserStoryEffort");
        userStory.getCharacteristicDefinition().add(userStoryEffort);
        Characteristic difficulty = super.buildDirectMeasure(vdm, "Difficulty");
        userStory.getCharacteristicDefinition().add(difficulty);
        Characteristic userStorySize = super.buildBinaryMeasure(vdm, "UserStorySize", difficulty, userStoryEffort, BinaryFunctor.MULTIPLY);
        userStory.getCharacteristicDefinition().add(userStorySize);

        CapabilityMethod implementUserStoryMethod = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(implementUserStoryMethod);
        implementUserStoryMethod.setName("ImplementUserStory");
        CapabilityDefinition detailUserStory = super.createCapabilityDefinition(vdm, "DetailUserStory");
        Role productOwnerRole = createRole(implementUserStoryMethod, "ProductOwner");
        Activity detailUserStoryActivity = addActivity(detailUserStory, implementUserStoryMethod, productOwnerRole, "DetailUserStory");
        implementUserStoryMethod.setInitialActivity(detailUserStoryActivity);
        CapabilityDefinition codeUserStory = super.createCapabilityDefinition(vdm, "CodeUserStory");
        Role developerRole = createRole(implementUserStoryMethod, "Developer");
        Activity codeUserStoryActivity = addActivity(codeUserStory, implementUserStoryMethod, developerRole, "CodeUserStory");
        BusinessItem userStoryItem = addBusinessItem(userStory, implementUserStoryMethod);
        DeliverableFlow userStoryFlow = addDeliverableFlow(implementUserStoryMethod, userStoryItem, detailUserStoryActivity, codeUserStoryActivity, "userStoryProvided", "userStoryReceived");
        BusinessItemDefinition money = createBusinessItemDefinition(vdm, "Money");
        StoreDefinition account = super.createStore(vdm, money, "Account");
        SupplyingStore fromAccount = addSupplyingStore(implementUserStoryMethod, account, productOwnerRole, "FromAccount", "Balance");
        SupplyingStore toAccount = addSupplyingStore(implementUserStoryMethod, account, developerRole, "ToAccount", "Balance");
        BusinessItem moneyItem = addBusinessItem(money, implementUserStoryMethod);
        DeliverableFlow moneyFlow = addDeliverableFlow(implementUserStoryMethod, moneyItem, fromAccount, codeUserStoryActivity, "moneyPaid", "moneyReceived");
        moneyFlow.getRecipient().setBatchSize(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        moneyFlow.getRecipient().getBatchSize().setCharacteristicDefinition(super.buildRescaledMeasure(vdm, "UserStoryPrice", userStorySize, 100d, 2d));
        vdm.eResource().save(new ByteArrayOutputStream(), null);

        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = new VdmlImporter(getEntityManager()).findCollaboration(MetaBuilder.buildUri(implementUserStoryMethod));
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant developer = participantService.createIndividualParticipant("developer");
        participantService.setCapabilities(developer.getId(), Collections.singleton(MetaBuilder.buildUri(codeUserStory)));
        IndividualParticipant productOwner = participantService.createIndividualParticipant("productOwner");
        participantService.setCapabilities(productOwner.getId(), Collections.singleton(MetaBuilder.buildUri(detailUserStory)));
        ProjectService ps = new ProjectService(getEntityManager());
        CollaborationObservation project = ps.initiateProject(productOwner.getId(), MetaBuilder.buildUri(implementUserStoryMethod));
        BusinessItemObservation bio = project.findBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        bio.findMeasurement(bio.getBusinessItemDefinition().findMeasurement("UserStoryEffort")).setValue(12d);
        bio.findMeasurement(bio.getBusinessItemDefinition().findMeasurement("Difficulty")).setValue(9d);
        ps.flush();
        //WHEN

        new ValueCalculationService(getEntityManager()).doCollaborationObservations(project.getId());
        CollaborationObservation collaborationObservationFound = new ProjectService(getEntityManager()).findProject(project.getId());
        BusinessItemObservation businessItemFound = collaborationObservationFound.findBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        DirectedFlowObservation moneyFlowObservation = collaborationObservationFound.findDeliverableFlow(collaborationObservationFound.getCollaboration().findDeliverableFlow(moneyFlow.getName()));
        Double userStorySize1 = businessItemFound.findMeasurement(bio.getBusinessItemDefinition().findMeasurement("UserStorySize")).getValue();
        Double userStoryPrice = moneyFlowObservation.getQuantity().getValue();
        assertEquals(108d, userStorySize1, 0.01d);
        assertEquals(316d, userStoryPrice, 0.01d);

    }
}
