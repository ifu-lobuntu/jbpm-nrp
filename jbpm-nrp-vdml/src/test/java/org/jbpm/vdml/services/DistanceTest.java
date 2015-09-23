package org.jbpm.vdml.services;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jbpm.vdml.services.impl.LocationUtil;
import org.jbpm.vdml.services.impl.model.runtime.TestLocation;
import org.junit.Test;
import org.opengis.geometry.DirectPosition;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DistanceTest extends MetaEntityImportTest {

    @Test
    public void testIt() throws Exception {
        GeometryFactory geometryFactory= JTSFactoryFinder.getGeometryFactory(null);
        EntityManager entityManager = getEntityManager();
        entityManager.persist(new TestLocation(geometryFactory.createPoint(new Coordinate(-25.978230, 27.690044))));
        entityManager.persist(new TestLocation(geometryFactory.createPoint(new Coordinate(-25.968230, 27.690044))));
        entityManager.persist(new TestLocation(geometryFactory.createPoint(new Coordinate(-25.968230, 27.620044))));
        entityManager.persist(new TestLocation(geometryFactory.createPoint(new Coordinate(-25.960000, 27.580044))));
        entityManager.persist(new TestLocation(geometryFactory.createPoint(new Coordinate(-25.958408, 27.534896))));
        entityManager.flush();
        Query q = entityManager.createQuery(" select distance(tl.location, :to) from TestLocation  tl where distance(tl.location,:to) < :distance");
        q.setParameter("to", geometryFactory.createPoint(new Coordinate(-25.978230, 27.690044)));
        q.setParameter("distance", LocationUtil.meterToEstimatedDegrees(19000));
        List<Number> resultList = q.getResultList();
        for (Number number : resultList) {
            System.out.println(LocationUtil.degreesToEstimatedMeters(number.doubleValue()));
        }
        DirectPosition fromPosition = JTS.toDirectPosition(new Coordinate(-25.978230, 27.690044), DefaultGeographicCRS.WGS84);
        DirectPosition toPosition = JTS.toDirectPosition(new Coordinate(-25.958408, 27.534896), DefaultGeographicCRS.WGS84);
//        Point p11 = JTS.toGeometry(fromPosition);
//        Point p12 = JTS.toGeometry(toPosition);
//        System.out.println(p11.distance(p12));
        Point p1 = geometryFactory.createPoint(new Coordinate(-25.978230, 27.690044));
        Point p2 = geometryFactory.createPoint(new Coordinate(-25.958408, 27.534896));
//        System.out.println(p1.distance(p2));
//        GeodeticCalculator gc = new GeodeticCalculator();
//        gc.setStartingPosition(fromPosition);
//        gc.setDestinationPosition(toPosition);
//        double distanceInDegrees = p1.distance(p2);
//        System.out.println(degreesToEstimatedMeters(distanceInDegrees));
//        double distanceInMeter = gc.getOrthodromicDistance();
//        System.out.println(distanceInMeter);
//        System.out.println(distanceInDegrees);
//        System.out.println(meterToEstimatedDegrees(distanceInMeter));
//
    }

}
