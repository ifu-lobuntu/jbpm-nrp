package org.jbpm.vdml.services;

import org.jbpm.vdml.services.api.model.LinkedExternalObject;
import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.omg.smm.Accumulator;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ObservationCalculationTest extends MetaEntityImportTest {
    private CapabilityMethod implementUserStoryMethod;
    private CapabilityDefinition detailUserStories;
    private CapabilityDefinition codeUserStory;
    private DeliverableFlow incomeFlow;
    private IndividualParticipant productOwner;
    private IndividualParticipant developer;
    private DeliverableFlow profitFlow;
    private DeliverableFlow featureFlow;
    private DeliverableFlow iterationPlanFlow;
    private Activity codeUserStoryActivity;
    private BusinessItemDefinition userStory;
    private BusinessItemDefinition iterationPlan;
    private DeliverableFlow userStoryFlow;
    private Activity detailUserStoryActivity;

    @Before
    public void setupModel() throws Exception {
        /**
         * Model: The ImplementUserStory entails detailing a UserSTory and the Implementing it.
         */
        ValueDeliveryModel vdm = buildModel();
        userStory = super.createBusinessItemDefinition(vdm, "UserStory");
        iterationPlan = super.createBusinessItemDefinition(vdm, "IterationPlan");
        /**
         * UserStorySize =Difficulty X UserStoryEffort
         */
        Characteristic userStoryEffort = super.buildDirectMeasure(vdm, "UserStoryEffort");
        Characteristic difficulty = super.buildDirectMeasure(vdm, "Difficulty");
        Characteristic userStorySize = super.buildBinaryMeasure(vdm, "UserStorySize", difficulty, userStoryEffort, BinaryFunctor.MULTIPLY);
        addCharacteristics(userStory.getCharacteristicDefinition(), userStoryEffort, difficulty, userStorySize);
        /**
         * A ReleasePlan is a store of IterationPlans
         */
        StoreDefinition releasePlan = createStore(vdm, iterationPlan, "ReleasePlan");

        implementUserStoryMethod = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(implementUserStoryMethod);
        implementUserStoryMethod.setName("ImplementUserStory");
        detailUserStories = super.createCapabilityDefinition(vdm, "DetailUserStories");
        Role productOwnerRole = createRole(implementUserStoryMethod, "ProductOwner");
        detailUserStoryActivity = addActivity(detailUserStories, implementUserStoryMethod, productOwnerRole, "DetailUserStories");
        implementUserStoryMethod.setInitialActivity(detailUserStoryActivity);
        codeUserStory = super.createCapabilityDefinition(vdm, "CodeUserStory");
        Role developerRole = createRole(implementUserStoryMethod, "Developer");
        codeUserStoryActivity = addActivity(codeUserStory, implementUserStoryMethod, developerRole, "CodeUserStory");

        BusinessItemDefinition executableFeature = createBusinessItemDefinition(vdm, "ExecutableFeature");
        StoreDefinition executableProduct = super.createStore(vdm, executableFeature, "ExecutableProduct");


        Characteristic durationOverrun = buildNamedMeasure(vdm, "DurationOverrun");
        Characteristic completionLateness = buildNamedMeasure(vdm, "CompletionLateness");
        Characteristic startDelay = buildNamedMeasure(vdm, "StartDelay");
        /**
         * Penalty=DurationOverrun X 0.5
         */
        Characteristic penalty = buildRescaledMeasure(vdm, "Penalty", durationOverrun, 0d, 0.5d);//30 money units per hour

        BusinessItem userStoryItem = addBusinessItem(userStory, implementUserStoryMethod);
        userStoryFlow = addDeliverableFlow(implementUserStoryMethod, userStoryItem, detailUserStoryActivity, codeUserStoryActivity, "userStoryProvided", "userStoryReceived");
        BusinessItemDefinition money = createBusinessItemDefinition(vdm, "Money");

        StoreDefinition account = super.createStore(vdm, money, "Account");
        account.setInventoryLevel(buildDirectMeasure(vdm, "Amount"));
        SupplyingStore fromAccount = addSupplyingStore(implementUserStoryMethod, account, productOwnerRole, "FromAccount", "Balance");
        SupplyingStore toAccount = addSupplyingStore(implementUserStoryMethod, account, developerRole, "ToAccount", "Balance");
        BusinessItem moneyItem = addBusinessItem(money, implementUserStoryMethod);
        incomeFlow = addDeliverableFlow(implementUserStoryMethod, moneyItem, fromAccount, codeUserStoryActivity, "moneyPaid", "moneyReceived");
        /**
         * UserStoryPrice = (UserStorySize X 2) + 100
         */
        Characteristic userStoryPrice = buildRescaledMeasure(vdm, "UserStoryPrice", userStorySize, 100d, 2d);
        incomeFlow.getProvider().setBatchSize(buildMeasuredCharacteristic(userStoryPrice));
        incomeFlow.getRecipient().setBatchSize(null);

        SupplyingStore iterationPlanStore = addSupplyingStore(implementUserStoryMethod, releasePlan, productOwnerRole, "ReleasePlan", "NoOfStories");
        this.iterationPlanFlow = addDeliverableFlow(implementUserStoryMethod, userStoryItem, iterationPlanStore, detailUserStoryActivity, "userStoryOut", "userStoryIn");
        addMeasuredCharacteristics(iterationPlanFlow.getMeasuredCharacteristic(), completionLateness);
        /**
         * LatenessDifficultyRatio = CompletionLateness / Difficulty
         */
        Characteristic latenessDifficultyRatio = buildBinaryMeasure(vdm, "LatenessDifficultyRatio", completionLateness, difficulty, BinaryFunctor.DIVIDE);
        addMeasuredCharacteristics(iterationPlanStore.getMeasuredCharacteristic(), latenessDifficultyRatio);
        ResourceUse moneyUse = VDMLFactory.eINSTANCE.createResourceUse();
        moneyUse.setName("MoneyUse");
        moneyUse.setInputDriven(true);
        moneyUse.getResource().add(incomeFlow.getRecipient());
        moneyUse.setResourceIsConsumed(true);
        moneyUse.setQuantity(buildMeasuredCharacteristic(buildDirectMeasure(vdm, "AmountUsed")));
        codeUserStoryActivity.getResourceUse().add(moneyUse);
        /**
         * TotalCost = Penalty + AmountUsed
         */
        Characteristic totalCost = super.buildBinaryMeasure(vdm, "TotalCost", penalty, moneyUse.getQuantity().getCharacteristicDefinition(), BinaryFunctor.PLUS);
        addMeasuredCharacteristics(codeUserStoryActivity.getMeasuredCharacteristic(), durationOverrun, completionLateness, startDelay, penalty, totalCost);
        Characteristic numberOfDifficultStories= super.buildCountingMeasure(vdm, "NumberOfDifficultStories", difficulty, "value > 6");
        addMeasuredCharacteristics(detailUserStoryActivity.getMeasuredCharacteristic(),numberOfDifficultStories);

        profitFlow = addDeliverableFlow(implementUserStoryMethod, moneyItem, codeUserStoryActivity, toAccount, "profitGenerated", "profitSaved");
        /**
         * Profit = UserStoryPrice - TotalCost
         */
        Characteristic profit = super.buildBinaryMeasure(vdm, "Profit", userStoryPrice, totalCost, BinaryFunctor.MINUS);
        profitFlow.getProvider().setBatchSize(buildMeasuredCharacteristic(profit));
        profitFlow.getRecipient().setBatchSize(null);

        /**
         * TotalProfit = SUM(Profit)
         */
        addMeasuredCharacteristics(toAccount.getMeasuredCharacteristic(), buildCollectiveMeasure(vdm, profit, "TotalProfit", Accumulator.SUM));


        SupplyingStore executableFeatureStore = addSupplyingStore(implementUserStoryMethod, executableProduct, productOwnerRole, "ExecutableProduct", "NumberOfFeatures");
        this.featureFlow = addDeliverableFlow(implementUserStoryMethod, addBusinessItem(executableFeature, implementUserStoryMethod), codeUserStoryActivity, executableFeatureStore, "ProducedFeature", "ProductIncrement");
        /**
         * TotalProfit = SUM(Profit)
         */
        addMeasuredCharacteristics(executableFeatureStore.getMeasuredCharacteristic(),buildCollectiveMeasure(vdm,profit,"TotalProfit"));

        OutputPort outputFeature = featureFlow.getProvider();
        ValueAdd ontimeDelivery = VDMLFactory.eINSTANCE.createValueAdd();
        /**
         * OnTimeDelivery = -(CompletionLateness)
         */
        ontimeDelivery.setValueMeasurement(buildMeasuredCharacteristic(buildRescaledMeasure(vdm, "OnTimeDelivery", completionLateness, 0, -1)));
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
        participantService.setCapabilities(productOwner.getId(), Collections.singleton(MetaBuilder.buildUri(detailUserStories)));

    }


    @Test
    public void testBusinessItemAndInputFlows() throws Exception {
        //WHEN
        ProjectService ps = new ProjectService(getEntityManager());
        CollaborationInstance project = ps.initiateProject(productOwner.getId(), MetaBuilder.buildUri(implementUserStoryMethod));
        BusinessItemObservation bio = project.findFirstBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        bio.findMeasurement(bio.getDefinition().findMeasure("UserStoryEffort")).setActualValue(12d);
        bio.findMeasurement(bio.getDefinition().findMeasure("Difficulty")).setActualValue(9d);
        ps.flush();

        Long projectId = project.getId();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(projectId);
        CollaborationInstance collaborationInstanceFound = new ProjectService(getEntityManager()).findProject(projectId);
        BusinessItemObservation businessItemFound = collaborationInstanceFound.findFirstBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        DeliverableFlowInstance moneyFlowObservation = collaborationInstanceFound.findFirstDeliverableFlow(collaborationInstanceFound.getCollaboration().findDeliverableFlow(incomeFlow.getName()));
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
        BusinessItemObservation bio = project.findFirstBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        bio.findMeasurement(bio.getDefinition().findMeasure("UserStoryEffort")).setActualValue(12d);
        bio.findMeasurement(bio.getDefinition().findMeasure("Difficulty")).setActualValue(9d);
        ActivityInstance codeUserStory = project.findFirstActivity(project.getCollaboration().findActivity("CodeUserStory"));
        codeUserStory.setPlannedStartDate(new DateTime(2015, 10, 1, 8, 0, 0, 0));
        codeUserStory.setActualStartDate(new DateTime(2015, 10, 1, 9, 0, 0, 0));//one hour late,
        codeUserStory.setPlannedDateOfCompletion(new DateTime(2015, 10, 1, 12, 0, 0, 0));
        codeUserStory.setActualDateOfCompletion(new DateTime(2015, 10, 1, 14, 0, 0, 0));//two hours late,one hour overrun
        codeUserStory.findResourceUse(codeUserStory.getActivity().findResourceUse("MoneyUse")).getQuantity().setActualValue(10d);
        ps.flush();

        Long projectId = project.getId();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(projectId);
        CollaborationInstance projecFound = new ProjectService(getEntityManager()).findProject(projectId);
        ActivityInstance codeUserStoryFound = projecFound.findFirstActivity(projecFound.getCollaboration().findActivity("CodeUserStory"));
        ActivityMeasurement totalCost = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("TotalCost"));
        assertEquals(10+30d, totalCost .getActualValue(), 0.01d);
        ActivityMeasurement overrun = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("DurationOverrun"));
        assertEquals(60d, overrun.getActualValue(), 0.01d);
        ActivityMeasurement completionLateness = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("CompletionLateness"));
        assertEquals(120d, completionLateness.getActualValue(), 0.01d);
        ActivityMeasurement startDelay = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("StartDelay"));
        assertEquals(60d, startDelay.getActualValue(), 0.01d);
        ActivityMeasurement penalty = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("Penalty"));
        assertEquals(30d, penalty.getActualValue(), 0.01d);
        DeliverableFlowInstance incomeFlowObservation = projecFound.findFirstDeliverableFlow(projecFound.getCollaboration().findDeliverableFlow(incomeFlow.getName()));
        assertEquals(316d, incomeFlowObservation.getQuantity().getActualValue(), 0.01d);
        DeliverableFlowInstance profitFlowObservation = projecFound.findFirstDeliverableFlow(projecFound.getCollaboration().findDeliverableFlow(profitFlow.getName()));
        assertEquals(316d - 10 - 30, profitFlowObservation.getSource().getBatchSize().getActualValue(), 0.01d);
        assertEquals(316d - 10 - 30, profitFlowObservation.getQuantity().getActualValue(), 0.01d);
        DeliverableFlowInstance featureFlow = projecFound.findFirstDeliverableFlow(projecFound.getCollaboration().findDeliverableFlow(this.featureFlow.getName()));
        ValueAddInstance onTimeDelivery = featureFlow.getSource().findValueAdd(featureFlow.getSource().getPort().findValueAdd("OnTimeDelivery"));
        assertEquals(-120d, onTimeDelivery.getValueMeasurement().getActualValue(), 0.01d);
    }
    @Test
    public void testMultiInstanceActivityAndResourceUseAndValueAdd() throws Exception {
        //WHEN
        ProjectService ps = new ProjectService(getEntityManager());
        CollaborationInstance project = ps.initiateProject(productOwner.getId(), MetaBuilder.buildUri(implementUserStoryMethod));
        ActivityInstance codeUserStory = project.findFirstActivity(project.getCollaboration().findActivity("CodeUserStory"));
        List<LinkedExternalObject> externalObjectList=new ArrayList<LinkedExternalObject>();
        externalObjectList.add(new LinkedExternalObject(MetaBuilder.buildUri(this.userStory),  "UserStory","Story0"));
        BusinessItemObservation bio = project.findFirstDeliverableFlow(project.getCollaboration().findDeliverableFlow(userStoryFlow.getName())).getDeliverable();
        bio.setLocalReference(new ExternalObjectReference("UserStory", "Story0"));
        int count=0;
        do {
            bio.findMeasurement(bio.getDefinition().findMeasure("UserStoryEffort")).setActualValue(12d);
            bio.findMeasurement(bio.getDefinition().findMeasure("Difficulty")).setActualValue(9d);
            codeUserStory.setPlannedStartDate(new DateTime(2015, 10, 1, 8, 0, 0, 0));
            codeUserStory.setActualStartDate(new DateTime(2015, 10, 1, 9, 0, 0, 0));//one hour late,
            codeUserStory.setPlannedDateOfCompletion(new DateTime(2015, 10, 1, 12, 0, 0, 0));
            codeUserStory.setActualDateOfCompletion(new DateTime(2015, 10, 1, 14, 0, 0, 0));//two hours late,one hour overrun
            codeUserStory.findResourceUse(codeUserStory.getActivity().findResourceUse("MoneyUse")).getQuantity().setActualValue(10d);
            ps.flush();
            count++;
            if(count<100) {
                LinkedExternalObject linkedExternalObject = new LinkedExternalObject(MetaBuilder.buildUri(this.userStory),  "UserStory","Story" + count);
                codeUserStory = ps.newActivity(project.getId(), MetaBuilder.buildUri(this.codeUserStoryActivity), "userStoryReceived", linkedExternalObject);
                bio=project.findBusinessItem(project.getCollaboration().findBusinessItem(userStory.getName()),linkedExternalObject.getIdentifier());
                externalObjectList.add(linkedExternalObject);
            }
        }while(count<100);
        Long projectId = project.getId();
        //WHEN
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(projectId);
        //THEN

        CollaborationInstance projecFound = new ProjectService(getEntityManager()).findProject(projectId);
        for (LinkedExternalObject linkedExternalObject : externalObjectList) {
            BusinessItemObservation userStoryInstance=projecFound.findBusinessItem(projecFound.getCollaboration().findBusinessItem(userStory.getName()),linkedExternalObject.getIdentifier());
            DeliverableFlowInstance userStoryFlowInstance=projecFound.findDeliverableFlow(projecFound.getCollaboration().findDeliverableFlow(userStoryFlow.getName()),userStoryInstance);
            ActivityInstance codeUserStoryFound = (ActivityInstance) userStoryFlowInstance.getTargetPortContainer();
            ActivityMeasurement totalCost = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("TotalCost"));
            assertEquals(10+30d, totalCost .getActualValue(), 0.01d);
            ActivityMeasurement overrun = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("DurationOverrun"));
            assertEquals(60d, overrun.getActualValue(), 0.01d);
            ActivityMeasurement completionLateness = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("CompletionLateness"));
            assertEquals(120d, completionLateness.getActualValue(), 0.01d);
            ActivityMeasurement startDelay = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("StartDelay"));
            assertEquals(60d, startDelay.getActualValue(), 0.01d);
            ActivityMeasurement penalty = codeUserStoryFound.findMeasurement(codeUserStoryFound.getActivity().findMeasure("Penalty"));
            assertEquals(30d, penalty.getActualValue(), 0.01d);
            DeliverableFlowInstance incomeFlowObservation = codeUserStoryFound.findInputPort(codeUserStoryFound.getActivity().findInputPort(incomeFlow.getRecipient().getName())).getInput();
            assertEquals(316d, incomeFlowObservation.getQuantity().getActualValue(), 0.01d);
            DeliverableFlowInstance profitFlowObservation = codeUserStoryFound.findOutputPort(codeUserStoryFound.getActivity().findOutputPort(profitFlow.getProvider().getName())).getOutput();
            assertEquals(316d - (10 + 30), profitFlowObservation.getSource().getBatchSize().getActualValue(), 0.01d);
            assertEquals(316d - (10 + 30)/*totalCost*/, profitFlowObservation.getQuantity().getActualValue(), 0.01d);
            DeliverableFlowInstance featureFlow = projecFound.findFirstDeliverableFlow(projecFound.getCollaboration().findDeliverableFlow(this.featureFlow.getName()));
            ValueAddInstance onTimeDelivery = featureFlow.getSource().findValueAdd(featureFlow.getDeliverableFlow().getSource().findValueAdd("OnTimeDelivery"));
            assertEquals(-120d, onTimeDelivery.getValueMeasurement().getActualValue(), 0.01d);
        }
        SupplyingStoreInstance toAccountFound= (SupplyingStoreInstance) projecFound.findPortContainer(project.getCollaboration().findSupplyingStore("ToAccount"));
        SupplyingStoreMeasurement totalProfit = toAccountFound.findMeasurement(toAccountFound.getSupplyingStore().findMeasure("TotalProfit"));
        assertEquals((316d - 10 - 30)*100, totalProfit.getActualValue(), 0.01d);
        ActivityInstance detailUserStoriesFound=projecFound.findFirstActivity(projecFound.getCollaboration().findActivity(detailUserStoryActivity.getName()));
        ActivityMeasurement numberOfDifficultStories=detailUserStoriesFound.findMeasurement(detailUserStoriesFound.getActivity().findMeasure("NumberOfDifficultStories"));
        assertEquals(100d,numberOfDifficultStories.getActualValue(),0.01d);
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
        BusinessItemObservation bio = project.findFirstBusinessItem(project.getCollaboration().findBusinessItem("UserStory"));
        bio.findMeasurement(bio.getDefinition().findMeasure("UserStoryEffort")).setActualValue(12d);
        bio.findMeasurement(bio.getDefinition().findMeasure("Difficulty")).setActualValue(9d);
        DeliverableFlowInstance flow = project.findFirstDeliverableFlow(project.getCollaboration().findDeliverableFlow(iterationPlanFlow.getName()));
        flow.setPlannedDate(new DateTime(2015, 10, 1, 8, 0, 0, 0));
        flow.setActualDate(new DateTime(2015, 10, 1, 8, 50, 0, 0));//50 minutes late,
        ps.flush();

        Long projectId = project.getId();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(projectId);
        CollaborationInstance projectFound = new ProjectService(getEntityManager()).findProject(projectId);
        SupplyingStoreInstance backlog = projectFound.findSupplyingStore(projectFound.getCollaboration().findSupplyingStore("ReleasePlan"));
        SupplyingStoreMeasurement latenessDifficultyRatio = backlog.findMeasurement(backlog.getSupplyingStore().findMeasure("LatenessDifficultyRatio"));
        assertEquals(50 / 9d, latenessDifficultyRatio.getActualValue(), 0.01d);
    }

}
