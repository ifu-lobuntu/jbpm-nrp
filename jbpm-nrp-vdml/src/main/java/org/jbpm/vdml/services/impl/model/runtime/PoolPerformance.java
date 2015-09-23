package org.jbpm.vdml.services.impl.model.runtime;

import com.vividsolutions.jts.geom.Point;
import org.jbpm.vdml.services.impl.model.meta.PoolDefinition;
import org.jbpm.vdml.services.impl.model.scheduling.SchedulableObject;
import org.jbpm.vdml.services.impl.model.scheduling.Schedule;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class PoolPerformance extends StorePerformance implements SchedulableObject{
    @ManyToOne
    private Schedule schedule;
    @OneToMany
    private Set<ReusableBusinessItemPerformance> pooledResources =new HashSet<ReusableBusinessItemPerformance>();
    public PoolPerformance() {
    }

    public PoolPerformance(PoolDefinition storeDefinition, Participant owner) {
        super(storeDefinition, owner);
    }


    public Set<ReusableBusinessItemPerformance> getPooledResources() {
        return pooledResources;
    }


    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public Point getLocation() {
        return getAddress()==null?null:getAddress().getLocation();
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
