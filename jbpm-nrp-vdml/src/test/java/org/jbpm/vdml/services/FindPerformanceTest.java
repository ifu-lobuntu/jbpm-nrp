package org.jbpm.vdml.services;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jbpm.vdml.services.api.model.CriterionOperator;
import org.jbpm.vdml.services.api.model.MeasurementCriterion;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemRequirement;
import org.jbpm.vdml.services.impl.*;
import org.jbpm.vdml.services.impl.model.runtime.*;
import org.jbpm.vdml.services.impl.model.runtime.CapabilityOffer;
import org.junit.Test;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;

import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FindPerformanceTest extends MetaEntityImportTest{

    private CapabilityDefinition produceDefinition;

    @Test
    public void testFindValueProposition() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        this.produceDefinition = createCapabilityDefinition(vdm, "Produce");
        Characteristic measure1 = buildDirectMeasure(vdm, "Measure1");
        Characteristic measure2 = buildDirectMeasure(vdm, "Measure2");
        Characteristic measure3 = buildDirectMeasure(vdm, "Measure3");

        addCharacteristics(produceDefinition.getCharacteristicDefinition(), measure1, measure2, measure3);


        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);

        setMeasures(5d, 500d, "Five");//Not in all criteria
        setMeasures(6, 800d, "Six");//Good, second closest
        setMeasures(7, 2000d, "Seven");//Good, too far
        setMeasures(8, 300d, "Eight");//Good, closest
        setMeasures(9, 2000d, "Nine");//Not in all criteria
        //WHEN

        SupplierMatcher service = new SupplierMatcher(getEntityManager());
        Collection<MeasurementCriterion> criteria = new ArrayList<MeasurementCriterion>();
        criteria.add(new MeasurementCriterion(MetaBuilder.buildUri(measure1.getMeasure().get(0)), 5d, 9d));
        criteria.add(new MeasurementCriterion(MetaBuilder.buildUri(measure2.getMeasure().get(0)), CriterionOperator.GREATER_THAN, 5d));
        criteria.add(new MeasurementCriterion(MetaBuilder.buildUri(measure3.getMeasure().get(0)), CriterionOperator.LESS_THAN, 9d));
        ReusableBusinessItemRequirement r = new ReusableBusinessItemRequirement();
        r.setLattitude(0d);
        r.setLongitude(0d);
        r.setMaxDistanceInMeter(1000d);
        List<CapabilityOffer> result = service.findMatchingCapabilityOffer(MetaBuilder.buildUri(produceDefinition), r, criteria);
        assertEquals(2, result.size());
        assertEquals("Eight", ((IndividualParticipant)result.get(0).getParticipant()).getUserName());
        assertEquals("Six", ((IndividualParticipant)result.get(1).getParticipant()).getUserName());
    }

    protected void setMeasures(double value, double distance, String participantName) {
        EntityManager entityManager = getEntityManager();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        Point location = geometryFactory.createPoint(new Coordinate(0d, LocationUtil.meterToEstimatedDegrees(distance)));
        ParticipantService participantService = new ParticipantService(entityManager);
        IndividualParticipant producerParticipant = participantService.createIndividualParticipant(participantName);
        participantService.setAddress(producerParticipant.getId(), new Address(location));
        participantService.setCapabilities(producerParticipant.getId(), Arrays.asList(MetaBuilder.buildUri(produceDefinition)));

        CapabilityOffer providingPerformance = producerParticipant.getCapabilityOffers().iterator().next();
        providingPerformance.findMeasurement(providingPerformance.getCapability().findMeasure("Measure1")).setActualValue(value);
        providingPerformance.findMeasurement(providingPerformance.getCapability().findMeasure("Measure2")).setActualValue(value);
        providingPerformance.findMeasurement(providingPerformance.getCapability().findMeasure("Measure3")).setActualValue(value);
        entityManager.flush();
    }
}
