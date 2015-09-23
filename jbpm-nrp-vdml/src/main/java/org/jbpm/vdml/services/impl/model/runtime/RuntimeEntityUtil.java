package org.jbpm.vdml.services.impl.model.runtime;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.opengis.geometry.DirectPosition;

import java.util.Collection;

public class RuntimeEntityUtil {
    public static <T extends RuntimeEntity> T findMatchingRuntimeEntity(Collection<? extends T > source, MetaEntity metaEntity){
        for (T t : source) {
            if(t.getMetaEntity().getUri().equals(metaEntity.getUri())){
                return t;
            }
        }
        throw new IllegalArgumentException("Not found: " + metaEntity.getName());
    }
    public static Geometry wktToGeometry(String wktPoint) {
        WKTReader fromText = new WKTReader();
        Geometry geom = null;
        try {
            geom = fromText.read(wktPoint);
        } catch (ParseException e) {
            throw new RuntimeException("Not a WKT string:" + wktPoint);
        }
        return geom;
    }

    public static void main(String[] args)  throws Exception{
        GeometryFactory geometryFactory= JTSFactoryFinder.getGeometryFactory(null);
        DirectPosition fromPosition = JTS.toDirectPosition(new Coordinate(-25.978230, 27.690044), DefaultGeographicCRS.WGS84);
        DirectPosition toPosition = JTS.toDirectPosition(new Coordinate(-25.958408, 27.534896), DefaultGeographicCRS.WGS84);
        Point p11 = JTS.toGeometry(fromPosition);
        Point p12 = JTS.toGeometry(toPosition);
        System.out.println(p11.distance(p12));
        Point p1 = geometryFactory.createPoint(new Coordinate(-25.978230, 27.690044));
        Point p2 = geometryFactory.createPoint(new Coordinate(-25.958408, 27.534896));
        System.out.println(p1.distance(p2));
        GeodeticCalculator gc = new GeodeticCalculator();
        gc.setStartingPosition(fromPosition);
        gc.setDestinationPosition(toPosition);
////        System.out.println(p1.distance(p2));
        System.out.println(gc.getOrthodromicDistance());

    }
}
