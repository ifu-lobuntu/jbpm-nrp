package org.jbpm.vdml.services;

import org.jbpm.vdml.services.api.model.LinkedExternalObject;
import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.jbpm.vdml.services.impl.model.runtime.CapabilityOffer;
import org.joda.time.DateTime;
import org.junit.Test;
import org.omg.smm.Accumulator;
import org.omg.smm.BinaryFunctor;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;
import test.TestGradeMeasure;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PerformanceCalculationTest extends MetaEntityImportTest {
    private ParticipantService participantService;
    private IndividualParticipant ekke;
    private CapabilityMethod cm;

    @Test
    public void testCapabilityPerformance() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        CapabilityDefinition codeUserStory = createCapabilityDefinition(vdm, "CodeUserStory");
        this.cm = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cm);
        cm.setName("DoStuffs");
        Activity activity = addActivity(codeUserStory, cm, createRole(cm, "Developer"), "CodeUserStory");
        Characteristic duration = buildNamedMeasure(vdm, "Duration");
        cm.setInitialActivity(activity);
        Characteristic quality = buildTheGradeMeasure(vdm);
        addMeasuredCharacteristics(activity.getMeasuredCharacteristic(), duration, quality);
        Characteristic averageDuration=buildCollectiveMeasure(vdm, duration, "AverageDuration", Accumulator.AVERAGE);
        Characteristic goodCount=buildCountingMeasure(vdm, "GoodCount", quality, "value='GOOD'");
        Characteristic biggerThanHourCount=buildCountingMeasure(vdm, "BiggerThanHourCount", duration, "value>60");
        Characteristic horriblyConvolutedMeasure=buildBinaryMeasure(vdm, "HorriblyConvolutedMeasure", goodCount, biggerThanHourCount,BinaryFunctor.PLUS);
        addCharacteristics(codeUserStory.getCharacteristicDefinition(), averageDuration, goodCount, biggerThanHourCount, horriblyConvolutedMeasure);
        vdm.eResource().save(new ByteArrayOutputStream(),null );
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);

        this.participantService = new ParticipantService(getEntityManager());
        this.ekke = participantService.createIndividualParticipant("ekke");
        //When
        participantService.setCapabilities(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(codeUserStory)));
        completeProject(new DateTime(2015, 8, 8, 7, 0, 0, 0), new DateTime(2015, 8, 8, 9, 0, 0, 0), TestGradeMeasure.GOOD);
        completeProject(new DateTime(2015, 8, 8, 11, 0, 0, 0), new DateTime(2015, 8, 8, 11, 30, 0, 0), TestGradeMeasure.GOOD);
        completeProject(new DateTime(2015, 8, 8, 12, 0, 0, 0), new DateTime(2015, 8, 8, 12, 30, 0, 0), TestGradeMeasure.BAD);
        Long cpId = ekke.getCapabilityOffers().iterator().next().getId();
        PerformanceCalculationService service = new PerformanceCalculationService(getEntityManager());
        service.calculateCapabilityPerformance(cpId);
        CapabilityOffer capabilityOffer = service.findCapabilityPerformance(cpId);
        CapabilityMeasurement averageDurationMeasurement = capabilityOffer.findMeasurement(capabilityOffer.getCapability().findMeasure("AverageDuration"));
        assertEquals(60d, averageDurationMeasurement.getActualValue(),0.01);
        CapabilityMeasurement biggerThanHourCountMeasurement = capabilityOffer.findMeasurement(capabilityOffer.getCapability().findMeasure("BiggerThanHourCount"));
        assertEquals(1d, biggerThanHourCountMeasurement.getActualValue(),0.01);
        CapabilityMeasurement goodCountMeasurement = capabilityOffer.findMeasurement(capabilityOffer.getCapability().findMeasure("GoodCount"));
        assertEquals(2d, goodCountMeasurement.getActualValue(),0.01);
        CapabilityMeasurement horriblyConvolutedMeasurement = capabilityOffer.findMeasurement(capabilityOffer.getCapability().findMeasure("HorriblyConvolutedMeasure"));
        assertEquals(3d, horriblyConvolutedMeasurement.getActualValue(),0.01);
    }

    private void completeProject(DateTime from, DateTime to, TestGradeMeasure rating) {
        ProjectService projectService=new ProjectService(getEntityManager());
        CollaborationInstance project = projectService.initiateProject(ekke.getId(), MetaBuilder.buildUri(cm));
        ActivityInstance ao1 = project.getActivities().iterator().next();
        ao1.findMeasurement(ao1.getActivity().findMeasure("TestGradeMeasure")).setActualRating(rating);
        ao1.setActualStartDate(from);
        ao1.setActualDateOfCompletion(to);
        projectService.flush();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(project.getId());
    }
    @Test
    public void testStorePerformance() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        BusinessItemDefinition freshProduce=createBusinessItemDefinition(vdm, "FreshProduce");
        StoreDefinition freshProduceStore = createStore(vdm, freshProduce, "FreshProduceStore");
        CapabilityDefinition orderFreshProduce=createCapabilityDefinition(vdm, "OrderFreshProduce");
        CapabilityDefinition supplyFreshProduce=createCapabilityDefinition(vdm, "SupplyFreshProduce");
        this.cm = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cm);
        cm.setName("DoStuffs");
        Activity orderFreshProduceActivity=addActivity(orderFreshProduce, cm, createRole(cm, "Consumer"), "OrderFreshProduce");
        cm.setInitialActivity(orderFreshProduceActivity);
        Role farmer = createRole(cm, "Farmer");
        SupplyingStore supplyingFreshProduceStore = addSupplyingStore(cm, freshProduceStore, farmer, "SupplyFreshProduce", "InventoryLevel");
        Characteristic lateness = buildDirectMeasure(vdm, "Lateness");//Usually derived from outgoing flows
        Characteristic quality = buildTheGradeMeasure(vdm);//Usually derived from outgoing businessItems
        addMeasuredCharacteristics(supplyingFreshProduceStore.getMeasuredCharacteristic(), lateness, quality);
        Characteristic averageLateness=buildCollectiveMeasure(vdm, lateness, "AverageLateness", Accumulator.AVERAGE);
        Characteristic goodCount=buildCountingMeasure(vdm, "GoodCount", quality, "value='GOOD'");
        Characteristic laterThanHourCount=buildCountingMeasure(vdm, "LaterThanHourCount", lateness, "value>60");
        Characteristic horriblyConvolutedMeasure=buildBinaryMeasure(vdm, "HorriblyConvolutedMeasure", goodCount, laterThanHourCount,BinaryFunctor.PLUS);
        addCharacteristics(freshProduceStore.getCharacteristicDefinition(), averageLateness, goodCount, laterThanHourCount, horriblyConvolutedMeasure);
        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);

        this.participantService = new ParticipantService(getEntityManager());
        this.ekke = participantService.createIndividualParticipant("consumer");
        IndividualParticipant farmerParticipant = participantService.createIndividualParticipant("farmer");
        //When
        participantService.setCapabilities(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(orderFreshProduce)));
        participantService.setStores(farmerParticipant.getId(), Collections.singleton(MetaBuilder.buildUri(freshProduceStore)));
        Long spId = farmerParticipant.getOfferedStores().iterator().next().getId();
        completeStoreProject(120, TestGradeMeasure.GOOD, spId);
        completeStoreProject(30, TestGradeMeasure.GOOD, spId);
        completeStoreProject(30, TestGradeMeasure.BAD, spId);
        PerformanceCalculationService service = new PerformanceCalculationService(getEntityManager());
        service.calculateStorePerformance(spId);
        StorePerformance storePerformance = service.findStorePerformance(spId);
        StoreMeasurement averageLatenessMeasurement = storePerformance.findMeasurement(storePerformance.getStoreDefinition().findMeasure("AverageLateness"));
        assertEquals(60d, averageLatenessMeasurement.getActualValue(), 0.01);
        StoreMeasurement laterThanHourCountMeasurement = storePerformance.findMeasurement(storePerformance.getStoreDefinition().findMeasure("LaterThanHourCount"));
        assertEquals(1d, laterThanHourCountMeasurement.getActualValue(),0.01);
        StoreMeasurement goodCountMeasurement = storePerformance.findMeasurement(storePerformance.getStoreDefinition().findMeasure("GoodCount"));
        assertEquals(2d, goodCountMeasurement.getActualValue(), 0.01);
        StoreMeasurement horriblyConvolutedMeasurement = storePerformance.findMeasurement(storePerformance.getStoreDefinition().findMeasure("HorriblyConvolutedMeasure"));
        assertEquals(3d, horriblyConvolutedMeasurement.getActualValue(),0.01);
    }

    private void completeStoreProject(int lateness, TestGradeMeasure rating, Long storeId) {
        ProjectService projectService=new ProjectService(getEntityManager());
        CollaborationInstance project = projectService.initiateProject(ekke.getId(), MetaBuilder.buildUri(cm));
        projectService.assignStorePerformance(project.getId(), storeId);
        SupplyingStoreInstance ao1 = project.getSupplyingStores().iterator().next();
        ao1.findMeasurement(ao1.getSupplyingStore().findMeasure("TestGradeMeasure")).setActualRating(rating);
        ao1.findMeasurement(ao1.getSupplyingStore().findMeasure("Lateness")).setActualValue((double) lateness);
        projectService.flush();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(project.getId());
    }

    @Test
    public void testBusinessItemPerformance() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        BusinessItemDefinition tukTuk=createBusinessItemDefinition(vdm, "TukTuk");
        CapabilityDefinition orderTukTuk=createCapabilityDefinition(vdm, "OrderTukTuk");
        PoolDefinition tukTukFleet=createPool(vdm, tukTuk, "TukTukFleet");
        this.cm = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cm);
        cm.setName("DoStuffs");
        Activity orderFreshProduceActivity=addActivity(orderTukTuk, cm, createRole(cm, "Consumer"), "OrderFreshProduce");
        cm.setInitialActivity(orderFreshProduceActivity);
        addSupplyingPool(cm, tukTukFleet, createRole(cm, "TukTukProvider"),"TukTukFleet","NumberOfTukTuks" );
        addBusinessItem(tukTuk, cm);
        Characteristic lateness = buildDirectMeasure(vdm, "Lateness");//Usually derived from outgoing flows
        Characteristic quality = buildTheGradeMeasure(vdm);//Usually derived from outgoing businessItems
        Characteristic averageLateness=buildCollectiveMeasure(vdm, lateness, "AverageLateness", Accumulator.AVERAGE);
        Characteristic goodCount=buildCountingMeasure(vdm, "GoodCount", quality, "value='GOOD'");
        Characteristic laterThanHourCount=buildCountingMeasure(vdm, "LaterThanHourCount", lateness, "value>60");
        Characteristic horriblyConvolutedMeasure=buildBinaryMeasure(vdm, "HorriblyConvolutedMeasure", goodCount, laterThanHourCount,BinaryFunctor.PLUS);
        addCharacteristics(tukTuk.getCharacteristicDefinition(), lateness, quality, averageLateness, goodCount, laterThanHourCount, horriblyConvolutedMeasure);
        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);

        this.participantService = new ParticipantService(getEntityManager());
        this.ekke = participantService.createIndividualParticipant("consumer");
        IndividualParticipant tukTukFleetOwner = participantService.createIndividualParticipant("tukTukFleetOwner");
        //When
        participantService.setCapabilities(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(orderTukTuk)));
        participantService.setStores(tukTukFleetOwner.getId(), Collections.singleton(MetaBuilder.buildUri(tukTukFleet)));
        Long bipId = participantService.addResourceToStore(tukTukFleetOwner.getOfferedStores().iterator().next().getId(), new LinkedExternalObject(MetaBuilder.buildUri(tukTuk),"TukTuk","myTuktuk"));
        completeBusinessItemProject(120, TestGradeMeasure.GOOD, bipId);
        completeBusinessItemProject(30, TestGradeMeasure.GOOD, bipId);
        completeBusinessItemProject(30, TestGradeMeasure.BAD, bipId);
        PerformanceCalculationService service = new PerformanceCalculationService(getEntityManager());
        service.calculateReusableResourcePerformance(bipId);
        ReusableBusinessItemPerformance rbiPerformance = service.findReusableBusinessItemPerformance(bipId);
        ReusableBusinessItemMeasurement averageLatenessMeasurement = rbiPerformance.findMeasurement(rbiPerformance.getDefinition().findMeasure("AverageLateness"));
        assertEquals(60d, averageLatenessMeasurement.getActualValue(), 0.01);
        ReusableBusinessItemMeasurement laterThanHourCountMeasurement = rbiPerformance.findMeasurement(rbiPerformance.getDefinition().findMeasure("LaterThanHourCount"));
        assertEquals(1d, laterThanHourCountMeasurement.getActualValue(),0.01);
        ReusableBusinessItemMeasurement goodCountMeasurement = rbiPerformance.findMeasurement(rbiPerformance.getDefinition().findMeasure("GoodCount"));
        assertEquals(2d, goodCountMeasurement.getActualValue(), 0.01);
        ReusableBusinessItemMeasurement horriblyConvolutedMeasurement = rbiPerformance.findMeasurement(rbiPerformance.getDefinition().findMeasure("HorriblyConvolutedMeasure"));
        assertEquals(3d, horriblyConvolutedMeasurement.getActualValue(),0.01);
    }

    private void completeBusinessItemProject(int lateness, TestGradeMeasure rating, Long bipId) {
        ProjectService projectService=new ProjectService(getEntityManager());
        CollaborationInstance project = projectService.initiateProject(ekke.getId(), MetaBuilder.buildUri(cm));
        projectService.assignReusableBusinessItemPerformance(project.getId(), bipId);
        BusinessItemObservation ao1 = project.getBusinessItems().iterator().next();
        ao1.findMeasurement(ao1.getDefinition().findMeasure("TestGradeMeasure")).setActualRating(rating);
        ao1.findMeasurement(ao1.getDefinition().findMeasure("Lateness")).setActualValue((double) lateness);
        projectService.flush();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(project.getId());
    }

}