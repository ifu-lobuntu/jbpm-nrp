package org.jbpm.vdml.services.impl.model.scheduling;

import com.vividsolutions.jts.geom.Point;

public interface SchedulableObject {
    Schedule getSchedule();
    Point getLocation();
}
