package org.jbpm.vdml.services;

import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.junit.Test;
import org.omg.smm.Accumulator;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;
import org.omg.vdml.Activity;
import org.omg.vdml.DeliverableFlow;
import org.omg.vdml.Role;
import org.omg.vdml.ValueProposition;
import org.omg.vdml.ValuePropositionComponent;
import test.TestGradeMeasure;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

public class RolePerformanceTest extends MetaEntityImportTest{
    private DeliverableFlow flow;
    private IndividualParticipant consumerParticipant;
    private IndividualParticipant producerParticipant;
    private Role consumer;
    private CapabilityMethod cm;

    @Test
    public void testValuePropositionPerformance() throws Exception{
        ValueDeliveryModel vdm = buildModel();
        CapabilityDefinition produceDefinition = createCapabilityDefinition(vdm, "Produce");
        CapabilityDefinition comsumeDefinition = createCapabilityDefinition(vdm, "Consume");
        this.cm= createCapabilityMethod(vdm, "SomeCapabilityMethod");
        Role supplier = createRole(cm, "Supplier");
        this.consumer = createRole(cm, "Consumer");
        Activity produce =addActivity(produceDefinition, cm, supplier, "Produce");
        Activity consume =addActivity(comsumeDefinition, cm, consumer, "Consume");
        cm.setInitialActivity(produce);
        BusinessItem stuff  =addBusinessItem(createBusinessItemDefinition(vdm, "Stuff"), cm);
        this.flow = addDeliverableFlow(cm, stuff, produce, consume, "out", "in");
        Characteristic theGradeMeasure = buildTheGradeMeasure(vdm);
        ValueAdd theGradeMeasureValueAdd = addValueAdd(flow, theGradeMeasure);
        Characteristic size = buildDirectMeasure(vdm, "Size");
        ValueAdd sizeValueAdd = addValueAdd(flow, size);
        ValueProposition vp = addValueProposition(supplier, consumer, "TheProposition");
        Characteristic averageSize = buildCollectiveMeasure(vdm, size, "AverageSize", Accumulator.AVERAGE);
        Characteristic numberOfBad = buildCountingMeasure(vdm, "NumberOfBad", theGradeMeasure, "value = 'BAD'");
        ValuePropositionComponent sizeComponent = addComponent(vp, averageSize);
        sizeComponent.getAggregatedFrom().add(sizeValueAdd);
        ValuePropositionComponent numberOfBadComponent = addComponent(vp, numberOfBad);
        numberOfBadComponent.getAggregatedFrom().add(theGradeMeasureValueAdd);
        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        this.producerParticipant = participantService.createIndividualParticipant("Producer");
        this.consumerParticipant = participantService.createIndividualParticipant("Consumer");
        performProject(TestGradeMeasure.BAD, 3d);
        performProject(TestGradeMeasure.GOOD, 5d);
        performProject(TestGradeMeasure.BAD, 6d);
        performProject(TestGradeMeasure.BAD, 2d);
        //WHEN
        this.producerParticipant = (IndividualParticipant) new ParticipantService(getEntityManager()).findParticipant(this.producerParticipant.getId());
        RolePerformance providingPerformance = this.producerParticipant.getRolePerformances().iterator().next();

        ValuePropositionPerformance valuePropositionPerformance = providingPerformance.getProvidedValuePropositions().iterator().next();
        PerformanceCalculationService service = new PerformanceCalculationService(getEntityManager());
        service.calculateValueProposition(valuePropositionPerformance.getId());
        ValuePropositionPerformance foundPerformance= service.findValuePropositionPerformance(valuePropositionPerformance.getId());
        ValuePropositionComponentPerformance numberOfBadPerformance= foundPerformance.findComponent(valuePropositionPerformance.getValueProposition().findComponent("NumberOfBad"));
        assertEquals(3d, numberOfBadPerformance.findMeasurement(numberOfBadPerformance.getValuePropositionComponent().findMeasure("NumberOfBad")).getActualValue(),0.1);
        ValuePropositionComponentPerformance averageSizePerformance= foundPerformance.findComponent(valuePropositionPerformance.getValueProposition().findComponent("AverageSize"));
        assertEquals(4d, averageSizePerformance.findMeasurement(averageSizePerformance.getValuePropositionComponent().findMeasure("AverageSize")).getActualValue(),0.1);
    }
    @Test
    public void testRelationshipPerformance() throws Exception{
        ValueDeliveryModel vdm = buildModel();
        CapabilityDefinition produceDefinition = createCapabilityDefinition(vdm, "Produce");
        CapabilityDefinition comsumeDefinition = createCapabilityDefinition(vdm, "Consume");
        this.cm= createCapabilityMethod(vdm, "SomeCapabilityMethod");
        Role supplier = createRole(cm, "Supplier");
        this.consumer = createRole(cm, "Consumer");
        Activity produce =addActivity(produceDefinition, cm, supplier, "Produce");
        Activity consume =addActivity(comsumeDefinition, cm, consumer, "Consume");
        cm.setInitialActivity(produce);
        BusinessItem stuff  =addBusinessItem(createBusinessItemDefinition(vdm, "Stuff"), cm);
        this.flow = addDeliverableFlow(cm, stuff, produce, consume, "out", "in");
        Characteristic theGradeMeasure = buildTheGradeMeasure(vdm);
        ValueAdd theGradeMeasureValueAdd = addValueAdd(flow, theGradeMeasure);
        Characteristic size = buildDirectMeasure(vdm, "Size");
        ValueAdd sizeValueAdd = addValueAdd(flow, size);
        ValueProposition vp = addValueProposition(supplier, consumer, "TheProposition");
        Characteristic averageSize = buildCollectiveMeasure(vdm, size, "AverageSize", Accumulator.AVERAGE);
        Characteristic numberOfBad = buildCountingMeasure(vdm, "NumberOfBad", theGradeMeasure, "value = 'BAD'");
        ValuePropositionComponent sizeComponent = addComponent(vp, averageSize);
        sizeComponent.getAggregatedFrom().add(sizeValueAdd);
        ValuePropositionComponent numberOfBadComponent = addComponent(vp, numberOfBad);
        numberOfBadComponent.getAggregatedFrom().add(theGradeMeasureValueAdd);
        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        this.consumerParticipant = participantService.createIndividualParticipant("Consumer");
        //Generate some background noise first to ensure we only pick up stats from the relationship with "Producer"
        this.producerParticipant = participantService.createIndividualParticipant("NoiseProducer");
        new TrustRelationshipService(getEntityManager()).requestTrustRelationship(MetaBuilder.buildUri(vp), consumerParticipant.getId(),producerParticipant.getId());
        performProject(TestGradeMeasure.GOOD, 3d);
        performProject(TestGradeMeasure.GOOD, 5d);
        performProject(TestGradeMeasure.BAD, 6d);
        performProject(TestGradeMeasure.BAD, 2d);
        //Now generate the stats we will be measuring
        this.producerParticipant = new ParticipantService(getEntityManager()).createIndividualParticipant("Producer");
        new TrustRelationshipService(getEntityManager()).requestTrustRelationship(MetaBuilder.buildUri(vp),consumerParticipant.getId(),producerParticipant.getId());
        performProject(TestGradeMeasure.BAD, 13d);
        performProject(TestGradeMeasure.GOOD, 15d);
        performProject(TestGradeMeasure.BAD, 16d);
        performProject(TestGradeMeasure.BAD, 12d);
        //WHEN
        this.producerParticipant = (IndividualParticipant) new ParticipantService(getEntityManager()).findParticipant(this.producerParticipant.getId());
        RolePerformance providingPerformance = this.producerParticipant.getRolePerformances().iterator().next();

        TrustRelationship providedRelatiopnship = providingPerformance.getProvidedRelationships().iterator().next();
        PerformanceCalculationService service = new PerformanceCalculationService(getEntityManager());
        service.calculateRelationshipPerformance(providedRelatiopnship.getId());
        TrustRelationship foundPerformance= service.findRelationshipPerformance(providedRelatiopnship.getId());
        TrustRelationshipComponent numberOfBadPerformance= foundPerformance.findComponent(providedRelatiopnship.getValueProposition().findComponent("NumberOfBad"));
        assertEquals(3d, numberOfBadPerformance.findMeasurement(numberOfBadPerformance.getValuePropositionComponent().findMeasure("NumberOfBad")).getActualValue(),0.1);
        TrustRelationshipComponent averageSizePerformance= foundPerformance.findComponent(providedRelatiopnship.getValueProposition().findComponent("AverageSize"));
        assertEquals(14d, averageSizePerformance.findMeasurement(averageSizePerformance.getValuePropositionComponent().findMeasure("AverageSize")).getActualValue(),0.1);
    }

    protected void performProject(Enum<?> rating, double s) {
        ProjectService projectService = new ProjectService(getEntityManager());
        CollaborationInstance project = projectService.initiateProject(producerParticipant.getId(), MetaBuilder.buildUri(cm));
        projectService.assignParticipantToRole(consumerParticipant.getId(), project.getId(), MetaBuilder.buildUri(consumer));
        DeliverableFlowInstance fo = project.findFirstDeliverableFlow(project.getCollaboration().findDeliverableFlow(flow.getName()));
        fo.getSource().findValueAdd(fo.getDeliverableFlow().getSource().findValueAdd("TestGradeMeasure")).getValueMeasurement().setActualRating(rating);
        fo.getSource().findValueAdd(fo.getDeliverableFlow().getSource().findValueAdd("Size")).getValueMeasurement().setActualValue(s);
        projectService.flush();
    }


}
