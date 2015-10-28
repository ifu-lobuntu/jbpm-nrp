package org.jbpm.vdml.services;

import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.omg.smm.BinaryFunctor;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;
import org.omg.vdml.Activity;
import org.omg.vdml.BusinessItemDefinition;
import org.omg.vdml.DeliverableFlow;
import org.omg.vdml.OutputPort;
import org.omg.vdml.ResourceUse;
import org.omg.vdml.Role;
import org.omg.vdml.StoreDefinition;
import org.omg.vdml.SupplyingStore;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ObservationCalculationTest extends MetaEntityImportTest {
    private CapabilityMethod implementUserStoryMethod;
    private CapabilityDefinition detailUserStory;
    private CapabilityDefinition codeUserStory;
    private DeliverableFlow incomeFlow;
    private IndividualParticipant productOwner;
    private IndividualParticipant developer;
    private DeliverableFlow profitFlow;
    private DeliverableFlow featureFlow;
    private DeliverableFlow userStoryFromBacklogFlow;

    @Before
    public void setupModel() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        BusinessItemDefinition userStory = super.createBusinessItemDefinition(vdm, "UserStory");
        Characteristic userStoryEffort = super.buildDirectMeasure(vdm, "UserStoryEffort");
        userStory.getCharacteristicDefinition().add(userStoryEffort);
        Characteristic difficulty = super.buildDirectMeasure(vdm, "Difficulty");
        userStory.getCharacteristicDefinition().add(difficulty);
        Characteristic userStorySize = super.buildBinaryMeasure(vdm, "UserStorySize", difficulty, userStoryEffort, BinaryFunctor.MULTIPLY);
        userStory.getCharacteristicDefinition().add(userStorySize);

        StoreDefinition backlog = createStore(vdm, userStory, "Backlog");

        implementUserStoryMethod = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(implementUserStoryMethod);
        implementUserStoryMethod.setName("ImplementUserStory");
        detailUserStory = super.createCapabilityDefinition(vdm, "DetailUserStory");
        Role productOwnerRole = createRole(implementUserStoryMethod, "ProductOwner");
        Activity detailUserStoryActivity = addActivity(detailUserStory, implementUserStoryMethod, productOwnerRole, "DetailUserStory");
        implementUserStoryMethod.setInitialActivity(detailUserStoryActivity);
        codeUserStory = super.createCapabilityDefinition(vdm, "CodeUserStory");
        Role developerRole = createRole(implementUserStoryMethod, "Developer");
        Activity codeUserStoryActivity = addActivity(codeUserStory, implementUserStoryMethod, developerRole, "CodeUserStory");

        BusinessItemDefinition executableFeature = createBusinessItemDefinition(vdm, "ExecutableFeature");
        StoreDefinition executableProduct = super.createStore(vdm, executableFeature, "ExecutableProduct");


        Characteristic durationOverrun = buildNamedMeasure(vdm, "DurationOverrun");
        Characteristic completionLateness = buildNamedMeasure(vdm, "CompletionLateness");
        Characteristic startDelay = buildNamedMeasure(vdm, "StartDelay");
        Characteristic penalty = buildRescaledMeasure(vdm, "Penalty", durationOverrun, 0d, 0.5d);//30 money units per hour

        BusinessItem userStoryItem = addBusinessItem(userStory, implementUserStoryMethod);
        DeliverableFlow userStoryFlow = addDeliverableFlow(implementUserStoryMethod, userStoryItem, detailUserStoryActivity, codeUserStoryActivity, "userStoryProvided", "userStoryReceived");
        BusinessItemDefinition money = createBusinessItemDefinition(vdm, "Money");

        StoreDefinition account = super.createStore(vdm, money, "Account");
        account.setInventoryLevel(buildDirectMeasure(vdm, "Amount"));
        SupplyingStore fromAccount = addSupplyingStore(implementUserStoryMethod, account, productOwnerRole, "FromAccount", "Balance");
        SupplyingStore toAccount = addSupplyingStore(implementUserStoryMethod, account, developerRole, "ToAccount", "Balance");
        BusinessItem moneyItem = addBusinessItem(money, implementUserStoryMethod);
        incomeFlow = addDeliverableFlow(implementUserStoryMethod, moneyItem, fromAccount, codeUserStoryActivity, "moneyPaid", "moneyReceived");
        incomeFlow.getProvider().setBatchSize(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        Characteristic userStoryPrice = super.buildRescaledMeasure(vdm, "UserStoryPrice", userStorySize, 100d, 2d);
        incomeFlow.getProvider().getBatchSize().setCharacteristicDefinition(userStoryPrice);
        incomeFlow.getRecipient().setBatchSize(null);

        SupplyingStore supplyingBacklog = addSupplyingStore(implementUserStoryMethod, backlog, productOwnerRole, "Backlog", "NoOfStories");
        this.userStoryFromBacklogFlow = addDeliverableFlow(implementUserStoryMethod, userStoryItem, supplyingBacklog, detailUserStoryActivity, "userStoryOut", "userStoryIn");
        addMeasuredCharacteristics(userStoryFromBacklogFlow.getMeasuredCharacteristic(), completionLateness);
        Characteristic latenessDifficultyRatio = buildBinaryMeasure(vdm, "LatenessDifficultyRatio", completionLateness, difficulty, BinaryFunctor.DIVIDE);
        addMeasuredCharacteristics(supplyingBacklog.getMeasuredCharacteristic(), latenessDifficultyRatio);
        ResourceUse moneyUse = VDMLFactory.eINSTANCE.createResourceUse();
        moneyUse.setName("MoneyUse");
        moneyUse.getResource().add(incomeFlow.getRecipient());
        moneyUse.setResourceIsConsumed(true);
        moneyUse.setQuantity(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        moneyUse.getQuantity().setCharacteristicDefinition(buildDirectMeasure(vdm, "AmountUsed"));
        codeUserStoryActivity.getResourceUse().add(moneyUse);

        Characteristic totalCost = super.buildBinaryMeasure(vdm, "TotalCost", penalty, moneyUse.getQuantity().getCharacteristicDefinition(), BinaryFunctor.PLUS);
        addMeasuredCharacteristics(codeUserStoryActivity.getMeasuredCharacteristic(), durationOverrun, completionLateness, startDelay, penalty, totalCost);

        profitFlow = addDeliverableFlow(implementUserStoryMethod, moneyItem, codeUserStoryActivity, toAccount, "profitGenerated", "profitSaved");
        profitFlow.getProvider().setBatchSize(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        profitFlow.getProvider().getBatchSize().setCharacteristicDefinition(super.buildBinaryMeasure(vdm, "Profit", userStoryPrice, totalCost, BinaryFunctor.MINUS));
        profitFlow.getRecipient().setBatchSize(null);

        SupplyingStore executableFeatureStore = addSupplyingStore(implementUserStoryMethod, executableProduct, productOwnerRole, "ExecutableProduct", "NumberOfFeatures");
        this.featureFlow = addDeliverableFlow(implementUserStoryMethod, addBusinessItem(executableFeature, implementUserStoryMethod), codeUserStoryActivity, executableFeatureStore, "ProducedFeature", "ProductIncrement");
        OutputPort outputFeature = featureFlow.getProvider();
        ValueAdd ontimeDelivery = VDMLFactory.eINSTANCE.createValueAdd();
        ontimeDelivery.setValueMeasurement(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        ontimeDelivery.getValueMeasurement().setCharacteristicDefinition(buildRescaledMeasure(vdm, "OnTimeDelivery", completionLateness, 0, -1));
        outputFeature.getValueAdd().add(ontimeDelivery);
        ontimeDelivery.setName("OnTimeDelivery");
        vdm.eResource().save(new ByteArrayOutputStream(), null);
        //GIVEN

        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        org.jbpm.vdml.services.impl.model.meta.Collaboration collaboration = new VdmlImporter(getEntityManager()).findCollaboration(MetaBuilder.buildUri(implementUserStoryMethod));
        ParticipantService participantService = new ParticipantService(getEntityManager());
        developer = participantService.createIndividualParticipant("developer");
        participantService.setCapabilities(developer.getId(), Collections.singleton(MetaBuilder.buildUri(codeUserStory)));
        productOwner = participantService.createIndividualParticipant("productOwner");
        participantService.setCapabilities(productOwner.getId(), Collections.singleton(MetaBuilder.buildUri(detailUserStory)));

    }

    @Test
    public void testBusinessItemAndInputFlows() throws Exception {
        //WHEN
        ProjectService ps = new ProjectService(getEntityManager());
        CollaborationInstance project = ps.initiateProject(productOwner.getId(), MetaBuilder.buildUri(implementUserStoryMethod));
        BusinessItemObservation bio = project.findBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        bio.findMeasurement(bio.getDefinition().findMeasure("UserStoryEffort")).setActualValue(12d);
        bio.findMeasurement(bio.getDefinition().findMeasure("Difficulty")).setActualValue(9d);
        ps.flush();

        Long projectId = project.getId();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(projectId);
        CollaborationInstance collaborationInstanceFound = new ProjectService(getEntityManager()).findProject(projectId);
        BusinessItemObservation businessItemFound = collaborationInstanceFound.findBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        DeliverableFlowInstance moneyFlowObservation = collaborationInstanceFound.findDeliverableFlow(collaborationInstanceFound.getCollaboration().findDeliverableFlow(incomeFlow.getName()));
        Double userStorySize1 = businessItemFound.findMeasurement(businessItemFound.getDefinition().findMeasure("UserStorySize")).getActualValue();
        Double userStoryPrice = moneyFlowObservation.getQuantity().getActualValue();
        assertEquals(108d, userStorySize1, 0.01d);
        assertEquals(316d, userStoryPrice, 0.01d);

    }

    @Test
    public void testActivityAndResourceUseAndValueAdd() throws Exception {
        //WHEN
        ProjectService ps = new ProjectService(getEntityManager());
        CollaborationInstance project = ps.initiateProject(productOwner.getId(), MetaBuilder.buildUri(implementUserStoryMethod));
        BusinessItemObservation bio = project.findBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        bio.findMeasurement(bio.getDefinition().findMeasure("UserStoryEffort")).setActualValue(12d);
        bio.findMeasurement(bio.getDefinition().findMeasure("Difficulty")).setActualValue(9d);
        ActivityInstance codeUserStory = project.findActivity(project.getCollaboration().findActivity("CodeUserStory"));
        codeUserStory.setPlannedStartDate(new DateTime(2015, 10, 1, 8, 0, 0, 0));
        codeUserStory.setActualStartDate(new DateTime(2015, 10, 1, 9, 0, 0, 0));//one hour late,
        codeUserStory.setPlannedDateOfCompletion(new DateTime(2015, 10, 1, 12, 0, 0, 0));
        codeUserStory.setActualDateOfCompletion(new DateTime(2015, 10, 1, 14, 0, 0, 0));//two hours late,one hour overrun
        codeUserStory.findResourceUse(codeUserStory.getActivity().findResourceUse("MoneyUse")).getQuantity().setActualValue(10d);
        ps.flush();

        Long projectId = project.getId();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(projectId);
        CollaborationInstance projecFound = new ProjectService(getEntityManager()).findProject(projectId);
        ActivityInstance codeUserStoryFound = projecFound.findActivity(projecFound.getCollaboration().findActivity("CodeUserStory"));
        ActivityMeasurement overrun = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("DurationOverrun"));
        assertEquals(60d, overrun.getActualValue(), 0.01d);
        ActivityMeasurement completionLateness = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("CompletionLateness"));
        assertEquals(120d, completionLateness.getActualValue(), 0.01d);
        ActivityMeasurement startDelay = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("StartDelay"));
        assertEquals(60d, startDelay.getActualValue(), 0.01d);
        ActivityMeasurement penalty = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("Penalty"));
        assertEquals(30d, penalty.getActualValue(), 0.01d);
        DeliverableFlowInstance incomeFlowObservation = projecFound.findDeliverableFlow(projecFound.getCollaboration().findDeliverableFlow(incomeFlow.getName()));
        assertEquals(316d, incomeFlowObservation.getQuantity().getActualValue(), 0.01d);
        DeliverableFlowInstance profitFlowObservation = projecFound.findDeliverableFlow(projecFound.getCollaboration().findDeliverableFlow(profitFlow.getName()));
        assertEquals(316d - 10 - 30, profitFlowObservation.getQuantity().getActualValue(), 0.01d);
        DeliverableFlowInstance featureFlow = projecFound.findDeliverableFlow(projecFound.getCollaboration().findDeliverableFlow(this.featureFlow.getName()));
        ValueAddMeasurement onTimeDelivery = featureFlow.findValueAdd(((org.jbpm.vdml.services.impl.model.meta.DeliverableFlow)featureFlow.getDeliverableFlow()).findValueAdd("OnTimeDelivery"));
        assertEquals(-120d, onTimeDelivery.getActualValue(), 0.01d);
    }

    @Test
    public void testSupplyingStore() throws Exception {
        //TODO
        /**
         * Test that we can calculate these metrics from the following contexts:
         * 1. BusinessItemObservations NB!!
         * 2. Outgoing Deliverable flows (look at duration, lateness implementations)
         * 3. Resource Use
         */
        //WHEN
        ProjectService ps = new ProjectService(getEntityManager());
        CollaborationInstance project = ps.initiateProject(productOwner.getId(), MetaBuilder.buildUri(implementUserStoryMethod));
        BusinessItemObservation bio = project.findBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        bio.findMeasurement(bio.getDefinition().findMeasure("UserStoryEffort")).setActualValue(12d);
        bio.findMeasurement(bio.getDefinition().findMeasure("Difficulty")).setActualValue(9d);
        DeliverableFlowInstance flow = project.findDeliverableFlow(project.getCollaboration().findDeliverableFlow(userStoryFromBacklogFlow.getName()));
        flow.setPlannedDate(new DateTime(2015, 10, 1, 8, 0, 0, 0));
        flow.setActualDate(new DateTime(2015, 10, 1, 8, 50, 0, 0));//50 minutes late,
        ps.flush();

        Long projectId = project.getId();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(projectId);
        CollaborationInstance projectFound = new ProjectService(getEntityManager()).findProject(projectId);
        SupplyingStoreInstance backlog = projectFound.findSupplyingStore(projectFound.getCollaboration().findSupplyingStore("Backlog"));
        SupplyingStoreMeasurement latenessDifficultyRatio = backlog.findMeasurement(backlog.getSupplyingStore().findMeasure("LatenessDifficultyRatio"));
        assertEquals(50 / 9d, latenessDifficultyRatio.getActualValue(), 0.01d);
    }

}
