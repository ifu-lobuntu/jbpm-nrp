package org.jbpm.vdml.services;

import org.eclipse.emf.common.util.EList;
import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.omg.smm.*;
import org.omg.smm.Accumulator;
import org.omg.smm.BinaryFunctor;
import org.omg.vdml.*;
import org.omg.vdml.Activity;
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
        addMeasuredCharacteristics(activity.getMeasuredCharacteristic(), duration,quality);
        Characteristic averageDuration=buildCollectiveMeasure(vdm, duration, "AverageDuration", Accumulator.AVERAGE);
        Characteristic goodCount=buildCountingMeasure(vdm, "GoodCount", quality, "value='GOOD'");
        Characteristic biggerThanHourCount=buildCountingMeasure(vdm, "BiggerThanHourCount", duration, "value>60");
        addCharacteristics(codeUserStory.getCharacteristicDefinition(),averageDuration,goodCount,biggerThanHourCount);
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
        CapabilityPerformance capabilityPerformance = service.findCapabilityPerformance(cpId);
        CapabilityMeasurement averageDurationMeasurement = capabilityPerformance.findMeasurement(capabilityPerformance.getCapability().findMeasure("AverageDuration"));
        assertEquals(60d, averageDurationMeasurement.getActualValue(),0.01);
        CapabilityMeasurement biggerThanHourCountMeasurement = capabilityPerformance.findMeasurement(capabilityPerformance.getCapability().findMeasure("BiggerThanHourCount"));
        assertEquals(1d, biggerThanHourCountMeasurement.getActualValue(),0.01);
        CapabilityMeasurement goodCountMeasurement = capabilityPerformance.findMeasurement(capabilityPerformance.getCapability().findMeasure("GoodCount"));
        assertEquals(2d, goodCountMeasurement.getActualValue(),0.01);
    }

    private void completeProject(DateTime from, DateTime to, TestGradeMeasure rating) {
        ProjectService projectService=new ProjectService(getEntityManager());
        CollaborationObservation project = projectService.initiateProject(ekke.getId(), MetaBuilder.buildUri(cm));
        ActivityObservation ao1 = project.getActivities().iterator().next();
        ao1.findMeasurement(ao1.getActivity().findMeasure("TestGradeMeasure")).setActualRating(rating);
        ao1.setActualStartDate(from);
        ao1.setActualDateOfCompletion(to);
        projectService.flush();
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(project.getId());
    }


}