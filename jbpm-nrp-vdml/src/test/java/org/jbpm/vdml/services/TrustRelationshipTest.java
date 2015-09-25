package org.jbpm.vdml.services;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jbpm.vdml.services.api.model.CriterionOperator;
import org.jbpm.vdml.services.api.model.LocationCriterion;
import org.jbpm.vdml.services.api.model.MeasurementCriterion;
import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.junit.Test;
import org.omg.smm.Accumulator;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;
import test.TestGradeMeasure;

import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TrustRelationshipTest extends MetaEntityImportTest {

    private CapabilityMethod cm;
    private Role consumer;
    private DeliverableFlow flow;
    private IndividualParticipant consumerParticipant;
//TODO testConfirm, testReject, testFindMyPreferredSuppliers
    @Test
    public void testFindValueProposition() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        CapabilityDefinition produceDefinition = createCapabilityDefinition(vdm, "Produce");
        CapabilityDefinition comsumeDefinition = createCapabilityDefinition(vdm, "Consume");
        this.cm = createCapabilityMethod(vdm, "SomeCapabilityMethod");
        Role supplier = createRole(cm, "Supplier");
        this.consumer = createRole(cm, "Consumer");
        Activity produce = addActivity(produceDefinition, cm, supplier, "Produce");
        Activity consume = addActivity(comsumeDefinition, cm, consumer, "Consume");
        cm.setInitialActivity(produce);
        BusinessItem stuff = addBusinessItem(createBusinessItemDefinition(vdm, "Stuff"), cm);
        this.flow = addDeliverableFlow(cm, stuff, produce, consume, "out", "in");
        ValueProposition vp = addValueProposition(supplier, consumer, "TheProposition");
        Characteristic measure1 = buildDirectMeasure(vdm, "Measure1");
        ValuePropositionComponent component1 = addComponent(vp, measure1);
        Characteristic measure2 = buildDirectMeasure(vdm, "Measure2");
        ValuePropositionComponent component2 = addComponent(vp, measure2);
        Characteristic measure3 = buildDirectMeasure(vdm, "Measure3");
        ValuePropositionComponent component3 = addComponent(vp, measure3);
        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        this.consumerParticipant = participantService.createIndividualParticipant("Consumer");
        performProject(5d, 500d, "Five");//Not in all criteria
        performProject(6, 800d, "Six");//Good, second closest
        performProject(7, 2000d, "Seven");//Good, too far
        performProject(8, 300d, "Eight");//Good, closest
        performProject(9, 2000d, "Nine");//Not in all criteria
        //WHEN

        TrustRelationshipService service = new TrustRelationshipService(getEntityManager());
        Collection<MeasurementCriterion> criteria = new ArrayList<MeasurementCriterion>();
        criteria.add(new MeasurementCriterion(MetaBuilder.buildUri(measure1.getMeasure().get(0)), 5d, 9d));
        criteria.add(new MeasurementCriterion(MetaBuilder.buildUri(measure2.getMeasure().get(0)), CriterionOperator.GREATER_THAN, 5d));
        criteria.add(new MeasurementCriterion(MetaBuilder.buildUri(measure3.getMeasure().get(0)), CriterionOperator.LESS_THAN, 9d));
        List<ValuePropositionPerformance> result = service.findMatchingValueProposition(MetaBuilder.buildUri(vp), new LocationCriterion(0d, 0d, 1000d), criteria);
        assertEquals(2, result.size());
        assertEquals("Eight", ((IndividualParticipant)result.get(0).getProvider().getParticipant()).getUserName());
        assertEquals("Six", ((IndividualParticipant)result.get(1).getProvider().getParticipant()).getUserName());
    }

    protected void performProject(double value, double distance, String participantName) {
        EntityManager entityManager = getEntityManager();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        Point location = geometryFactory.createPoint(new Coordinate(0d, LocationUtil.meterToEstimatedDegrees(distance)));
        ParticipantService participantService = new ParticipantService(entityManager);
        IndividualParticipant producerParticipant = participantService.createIndividualParticipant(participantName);
        participantService.setAddress(producerParticipant.getId(), new Address(location));
        ProjectService projectService = new ProjectService(entityManager);
        projectService.initiateProject(producerParticipant.getId(), MetaBuilder.buildUri(cm));
        producerParticipant = (IndividualParticipant) participantService.findParticipant(producerParticipant.getId());
        RolePerformance providingPerformance = producerParticipant.getRolePerformances().iterator().next();
        ValuePropositionPerformance valueProposition = providingPerformance.getProvidedValuePropositions().iterator().next();
        ValuePropositionComponentPerformance component2 = valueProposition.findComponent(valueProposition.getValueProposition().findComponent("Measure1"));
        component2.findMeasurement(component2.getValuePropositionComponent().findMeasure("Measure1")).setActualValue(value);
        ValuePropositionComponentPerformance component1 = valueProposition.findComponent(valueProposition.getValueProposition().findComponent("Measure2"));
        component1.findMeasurement(component1.getValuePropositionComponent().findMeasure("Measure2")).setActualValue(value);
        ValuePropositionComponentPerformance component3 = valueProposition.findComponent(valueProposition.getValueProposition().findComponent("Measure3"));
        component3.findMeasurement(component3.getValuePropositionComponent().findMeasure("Measure3")).setActualValue(value);
        entityManager.flush();
    }
}
