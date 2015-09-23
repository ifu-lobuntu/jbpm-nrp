package org.jbpm.vdml.services.impl.model.runtime;


import com.vividsolutions.jts.geom.Point;
import org.jbpm.vdml.services.impl.model.meta.BusinessItemDefinition;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.scheduling.SchedulableObject;
import org.jbpm.vdml.services.impl.model.scheduling.Schedule;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ReusableBusinessItemPerformance implements ActivatableRuntimeEntity,SchedulableObject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private ReusableBusinessItemPerformance extendedReusableBusinessItemPerformance;
    @ManyToOne
    private BusinessItemDefinition definition;

    @ManyToOne
    private PoolPerformance hostingPool;

    @ManyToOne
    @JoinColumn(nullable = true)
    private Participant represents;
    @ManyToOne(cascade = CascadeType.ALL)
    private Schedule schedule;
    @ManyToOne(cascade = CascadeType.ALL)
    private Address address;

    @Embedded
    private ExternalObjectReference instanceReference=new ExternalObjectReference();

    @OneToMany()
    private Set<ReusableBusinessItemMeasurement> measurements=new HashSet<ReusableBusinessItemMeasurement>();//Aggregated from ActivityObservation.measurements

    public ReusableBusinessItemPerformance() {
    }

    public ReusableBusinessItemPerformance(BusinessItemDefinition definition, ExternalObjectReference instance) {
        this.definition = definition;
        this.instanceReference=instance;
    }

    public Participant getRepresents() {
        return represents;
    }

    public void setRepresents(Participant represents) {
        this.represents = represents;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getDefinition();
    }

    public BusinessItemDefinition getDefinition() {
        return definition;
    }

    public Set<ReusableBusinessItemMeasurement> getMeasurements() {
        return measurements;
    }

    public ExternalObjectReference getInstanceReference() {
        return instanceReference;
    }

    public void setInstanceReference(ExternalObjectReference instanceReference) {
        this.instanceReference = instanceReference;
    }

    public ReusableBusinessItemPerformance getExtendedReusableBusinessItemPerformance() {
        return extendedReusableBusinessItemPerformance;
    }

    public void setExtendedReusableBusinessItemPerformance(ReusableBusinessItemPerformance extendedReusableBusinessItemPerformance) {
        this.extendedReusableBusinessItemPerformance = extendedReusableBusinessItemPerformance;
    }

    public PoolPerformance getHostingPool() {
        return hostingPool;
    }

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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setHostingPool(PoolPerformance hostingPool) {
        if(this.hostingPool!=null) {
            this.hostingPool.getPooledResources().remove(this);
        }
        this.hostingPool = hostingPool;
        if(this.hostingPool!=null) {
            this.hostingPool.getPooledResources().add(this);
        }
    }
}

