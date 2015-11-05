package org.jbpm.vdml.services;

import org.jbpm.vdml.services.api.model.LinkedExternalObject;
import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.junit.Test;
import org.omg.smm.Accumulator;
import org.omg.smm.BinaryFunctor;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;
import org.omg.vdml.Activity;
import org.omg.vdml.BusinessItemDefinition;
import org.omg.vdml.CapabilityMethod;
import org.omg.vdml.DeliverableFlow;
import org.omg.vdml.Role;
import org.omg.vdml.StoreDefinition;
import org.omg.vdml.SupplyingStore;
import org.omg.vdml.ValueAdd;
import org.omg.vdml.ValueProposition;
import org.omg.vdml.ValuePropositionComponent;
import test.TestGradeMeasure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RolePerformanceTest extends MetaEntityImportTest {
    private DeliverableFlow flow;
    private IndividualParticipant consumerParticipant;
    private IndividualParticipant supplierParticipant;
    private Role consumer;
    private CapabilityMethod cm;
    private Role supplier;
    private ValueProposition networkProposition;

    @Test
    public void testValuePropositionPerformance() throws Exception {
        populateMetaData();
        ParticipantService participantService = new ParticipantService(getEntityManager());
        this.supplierParticipant = participantService.createIndividualParticipant("Provider");
        this.consumerParticipant = participantService.createIndividualParticipant("Consumer");
        performProject(TestGradeMeasure.BAD, 3d);
        performProject(TestGradeMeasure.GOOD, 5d);
        performProject(TestGradeMeasure.BAD, 6d);
        performProject(TestGradeMeasure.BAD, 2d);
        //WHEN
        this.supplierParticipant = (IndividualParticipant) new ParticipantService(getEntityManager()).findParticipant(this.supplierParticipant.getId());
        RolePerformance providingPerformance = this.supplierParticipant.getRolePerformances().iterator().next();

        ValuePropositionPerformance valuePropositionPerformance = providingPerformance.getProvidedValuePropositions().iterator().next();
        PerformanceCalculationService service = new PerformanceCalculationService(getEntityManager());
        service.calculateValueProposition(valuePropositionPerformance.getId());
        ValuePropositionPerformance foundPerformance = service.findValuePropositionPerformance(valuePropositionPerformance.getId());
        ValuePropositionComponentPerformance numberOfBadPerformance = foundPerformance.findComponent(valuePropositionPerformance.getValueProposition().findComponent("TotalNumberOfBad"));
        assertEquals(3d, numberOfBadPerformance.findMeasurement(numberOfBadPerformance.getValuePropositionComponent().findMeasure("TotalNumberOfBad")).getActualValue(), 0.1);
        ValuePropositionComponentPerformance averageSizePerformance = foundPerformance.findComponent(valuePropositionPerformance.getValueProposition().findComponent("AverageSize"));
        assertEquals(4d, averageSizePerformance.findMeasurement(averageSizePerformance.getValuePropositionComponent().findMeasure("AverageSize")).getActualValue(), 0.1);
    }

    protected void populateMetaData() throws IOException {
        ValueDeliveryModel vdm = buildModel();
        CapabilityDefinition provideDefinition = createCapabilityDefinition(vdm, "Provide");
        CapabilityDefinition comsumeDefinition = createCapabilityDefinition(vdm, "Consume");
        this.cm = createCapabilityMethod(vdm, "SomeCapabilityMethod");
        OrgUnit valueNetwork = createValueNetwork(vdm, "ValueNetwork");
        this.supplier = createRole(cm, valueNetwork, "Supplier");
        this.consumer = createRole(cm, valueNetwork, "Consumer");
        Activity provide = addActivity(provideDefinition, cm, supplier, "Provide");
        Activity consume = addActivity(comsumeDefinition, cm, consumer, "Consume");
        cm.setInitialActivity(provide);
        BusinessItemDefinition stuffDefinition = createBusinessItemDefinition(vdm, "Stuff");
        BusinessItem stuff = addBusinessItem(stuffDefinition, cm);
        StoreDefinition stuffStoreDefinition = createStore(vdm, stuffDefinition, "StuffStore");
        SupplyingStore stuffSupplyingStore = addSupplyingStore(cm, stuffStoreDefinition, supplier, "StuffStore", "StuffInventoryLevel");
        addDeliverableFlow(cm, stuff, stuffSupplyingStore, provide, "out", "in");
        this.flow = addDeliverableFlow(cm, stuff, provide, consume, "out", "in");
        Characteristic theGradeMeasure = buildTheGradeMeasure(vdm);
        ValueAdd theGradeMeasureValueAdd = addValueAdd(flow, theGradeMeasure);
        Characteristic size = buildDirectMeasure(vdm, "Size");
        ValueAdd sizeValueAdd = addValueAdd(flow, size);
        ValueProposition capabilityMethodProposition = addValueProposition(supplier, consumer, "TheProposition");
        Characteristic totalSize = buildCollectiveMeasure(vdm, size, "TotalSize", Accumulator.SUM);
        Characteristic totalCount = buildCountingMeasure(vdm, "TotalCount", size, null);
        Characteristic numberOfBad = buildCountingMeasure(vdm, "NumberOfBad", theGradeMeasure, "value = 'BAD'");
        ValuePropositionComponent totalSizeComponent = addComponent(capabilityMethodProposition, totalSize);
        totalSizeComponent.getAggregatedFrom().add(sizeValueAdd);
        ValuePropositionComponent totalCountComponent = addComponent(capabilityMethodProposition, totalCount);
        totalCountComponent.getAggregatedFrom().add(sizeValueAdd);
        ValuePropositionComponent numberOfBadComponent = addComponent(capabilityMethodProposition, numberOfBad);
        numberOfBadComponent.getAggregatedFrom().add(theGradeMeasureValueAdd);

        this.networkProposition = addValueProposition((Role) supplier.getRoleAssignment().get(0).getParticipant(), (Role) consumer.getRoleAssignment().get(0).getParticipant(), "TheProposition");
        Characteristic totalNumberOfBad = buildCollectiveMeasure(vdm, numberOfBad, "TotalNumberOfBad", Accumulator.SUM);
        ValuePropositionComponent totalNumberOfBadComponent = addComponent(networkProposition, totalNumberOfBad);
        totalNumberOfBadComponent.getAggregatedFrom().add(numberOfBadComponent);
        Characteristic grandTotalSize = buildCollectiveMeasure(vdm, totalSize, "GrandTotalSize", Accumulator.SUM);
        Characteristic grandTotalCount = buildCollectiveMeasure(vdm, totalCount, "GrandTotalSize", Accumulator.SUM);
        Characteristic averageSize = buildBinaryMeasure(vdm, "AverageSize", grandTotalSize, grandTotalCount, BinaryFunctor.DIVIDE);
        ValuePropositionComponent averageSizeComponent = addComponent(networkProposition, averageSize);
        averageSizeComponent.getAggregatedFrom().add(totalSizeComponent);
        averageSizeComponent.getAggregatedFrom().add(totalCountComponent);
        addMeasuredCharacteristics(averageSizeComponent.getMeasuredCharacteristic(),grandTotalCount,grandTotalSize);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
    }

        @Test
    public void testRelationshipPerformance() throws Exception {
        populateMetaData();
        ParticipantService participantService = new ParticipantService(getEntityManager());
        this.consumerParticipant = participantService.createIndividualParticipant("Consumer");
        //Generate some background noise first to ensure we only pick up stats from the relationship with "Provider"
        this.supplierParticipant = participantService.createIndividualParticipant("NoiseProvider");
        new TrustRelationshipService(getEntityManager()).requestTrustRelationship(MetaBuilder.buildUri(networkProposition), consumerParticipant.getId(), supplierParticipant.getId());
        performProject(TestGradeMeasure.GOOD, 3d);
        performProject(TestGradeMeasure.GOOD, 5d);
        performProject(TestGradeMeasure.BAD, 6d);
        performProject(TestGradeMeasure.BAD, 2d);
        //Now generate the stats we will be measuring
        this.supplierParticipant = new ParticipantService(getEntityManager()).createIndividualParticipant("Provider");
        new TrustRelationshipService(getEntityManager()).requestTrustRelationship(MetaBuilder.buildUri(networkProposition), consumerParticipant.getId(), supplierParticipant.getId());
        performProject(TestGradeMeasure.BAD, 13d);
        performProject(TestGradeMeasure.GOOD, 15d);
        performProject(TestGradeMeasure.BAD, 16d);
        performProject(TestGradeMeasure.BAD, 12d);
        //WHEN
        this.supplierParticipant = (IndividualParticipant) new ParticipantService(getEntityManager()).findParticipant(this.supplierParticipant.getId());
        RolePerformance providingPerformance = this.supplierParticipant.getRolePerformances().iterator().next();

        TrustRelationship providedRelatiopnship = providingPerformance.getProvidedRelationships().iterator().next();
        PerformanceCalculationService service = new PerformanceCalculationService(getEntityManager());
        service.calculateRelationshipPerformance(providedRelatiopnship.getId());
        TrustRelationship foundPerformance = service.findRelationshipPerformance(providedRelatiopnship.getId());
        TrustRelationshipComponent numberOfBadPerformance = foundPerformance.findComponent(providedRelatiopnship.getValueProposition().findComponent("TotalNumberOfBad"));
        assertEquals(3d, numberOfBadPerformance.findMeasurement(numberOfBadPerformance.getValuePropositionComponent().findMeasure("TotalNumberOfBad")).getActualValue(), 0.1);
        TrustRelationshipComponent totalSizePerformance = foundPerformance.findComponent(providedRelatiopnship.getValueProposition().findComponent("AverageSize"));
        assertEquals(14d, totalSizePerformance.findMeasurement(totalSizePerformance.getValuePropositionComponent().findMeasure("AverageSize")).getActualValue(), 0.1);
    }

    protected void performProject(Enum<?> rating, double s) {
        ProjectService projectService = new ProjectService(getEntityManager());
        CollaborationInstance project = projectService.initiateProject(supplierParticipant.getId(), MetaBuilder.buildUri(cm));
        DeliverableFlowInstance dfi = project.findFirstDeliverableFlow(project.getCollaboration().findDeliverableFlow(flow.getName()));
        org.jbpm.vdml.services.impl.model.meta.DeliverableFlow df = dfi.getDeliverableFlow();
        org.jbpm.vdml.services.impl.model.meta.ValueAdd testGradeMeasure = df.getSource().findValueAdd("TestGradeMeasure");
        dfi.getSource().findValueAdd(testGradeMeasure).getValueMeasurement().setActualRating(rating);
        org.jbpm.vdml.services.impl.model.meta.ValueAdd size = df.getSource().findValueAdd("Size");
        dfi.getSource().findValueAdd(size).getValueMeasurement().setActualValue(s);
        dfi.getDeliverable().setLocalReference(new ExternalObjectReference("Stuff", "Stuff0"));
        projectService.flush();
        for (int i = 1; i <= 5; i++) {
            LinkedExternalObject stuff = new LinkedExternalObject(df.getDeliverable().getUri(), "Stuff", "Stuff" + i);
            org.jbpm.vdml.services.impl.model.meta.PortContainer provideActivity = df.getSource().getPortContainer();
            ActivityInstance ai = projectService.newActivity(project.getId(), provideActivity.getUri(), "in", stuff);
            ai.findOutputPort(df.getSource()).findValueAdd(testGradeMeasure).getValueMeasurement().setActualRating(TestGradeMeasure.AVERAGE);//So as not to affect the count of BAD 'rating'
            ai.findOutputPort(df.getSource()).findValueAdd(size).getValueMeasurement().setActualValue(s + (i - 3));//'s' remains the average
            projectService.flush();
        }
        projectService.assignParticipantToRole(consumerParticipant.getId(), project.getId(), MetaBuilder.buildUri(consumer));
        projectService.assignParticipantToRole(supplierParticipant.getId(), project.getId(), MetaBuilder.buildUri(supplier));
        new ObservationCalculationService(getEntityManager()).resolveCollaborationMeasurements(project.getId());
        CollaborationInstance foundProject = new ProjectService(getEntityManager()).findProject(project.getId());
        assertEquals(1, foundProject.getValuePropositions().size());
        ValuePropositionInstance vpi = foundProject.getValuePropositions().iterator().next();
        assertEquals(3, vpi.getComponents().size());
        ValuePropositionComponentInstanceMeasurement totalSize = vpi.findComponent(vpi.getValueProposition().findComponent("TotalSize")).getValueMeasurement();
        assertEquals(s * 6, totalSize.getActualValue(), 0.1d);


        ValuePropositionComponentInstanceMeasurement totalCount = vpi.findComponent(vpi.getValueProposition().findComponent("TotalCount")).getValueMeasurement();
        assertEquals(6, totalCount.getActualValue(), 0.1d);
        ValuePropositionComponentInstanceMeasurement numberOfBad = vpi.findComponent(vpi.getValueProposition().findComponent("NumberOfBad")).getValueMeasurement();
        if (TestGradeMeasure.BAD == rating) {
            assertEquals(1, numberOfBad.getActualValue(), 0.1d);
        } else {
            assertEquals(0, numberOfBad.getActualValue(), 0.1d);
        }
    }


}
