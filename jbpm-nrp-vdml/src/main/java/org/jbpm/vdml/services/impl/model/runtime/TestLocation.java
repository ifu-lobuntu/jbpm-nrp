package org.jbpm.vdml.services.impl.model.runtime;

import com.vividsolutions.jts.geom.Point;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
public class TestLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Type(type="org.hibernate.spatial.GeometryType")
    private Point location;

    public TestLocation() {
    }

    public TestLocation(Point location) {

        this.location = location;
    }
}
