package org.jbpm.vdml.services.impl.model.runtime;

import com.vividsolutions.jts.geom.Point;
import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ResourceUse;
import org.jbpm.vdml.services.impl.model.scheduling.PlannedUnavailability;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ResourceUseObservation implements RuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private ResourceUse resourceUse;
    @ManyToOne
    private ActivityObservation activity;
    @ManyToOne
    private DirectedFlowObservation input;
    @ManyToOne
    private DirectedFlowObservation output;
    @ManyToOne
    private Address address;

    @ManyToOne
    private PoolPerformance pool;
    @ManyToOne
    private ReusableBusinessItemPerformance reusableResource;

    @Column(name = "`from`")
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime from;

    @Column(name = "`to`")
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime to;
    @ManyToOne
    private PlannedUnavailability plannedUnavailability;

    private double quantity;


    @OneToMany(mappedBy = "resourceUse")
    private Set<ResourceUseMeasurement> measurements = new HashSet<ResourceUseMeasurement>();
    @Enumerated
    private ValueFlowStatus status;

    public ResourceUseObservation() {
    }
    public ResourceUseObservation(ResourceUse resourceUse, ActivityObservation activity) {
        this.resourceUse = resourceUse;
        this.activity = activity;
        this.activity.getResourceUseObservation().add(this);
    }


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public PlannedUnavailability getPlannedUnavailability() {
        return plannedUnavailability;
    }

    public void setPlannedUnavailability(PlannedUnavailability plannedUnavailability) {
        this.plannedUnavailability = plannedUnavailability;
    }

    public ReusableBusinessItemPerformance getReusableResource() {
        return reusableResource;
    }

    public void setReusableResource(ReusableBusinessItemPerformance nonFungibleResource) {
        this.reusableResource = nonFungibleResource;
    }


    public ResourceUseMeasurement getQuantity() {
        return getMeasurement(this.resourceUse.getQuantity());
    }

    public ResourceUseMeasurement getDuration() {
        return getMeasurement(this.resourceUse.getDuration());
    }

    private ResourceUseMeasurement getMeasurement(Measure m) {
        for (ResourceUseMeasurement measurement : measurements) {
            if (measurement.getMeasure().equals(m)) {
                return measurement;
            }
        }
        return null;
    }

    public DateTime getFrom() {
        return from;
    }

    public void setFrom(DateTime from) {
        this.from = from;
    }

    public DateTime getTo() {
        return to;
    }

    public void setTo(DateTime to) {
        this.to = to;
    }

    public PoolPerformance getPool() {
        return pool;
    }

    public void setPool(PoolPerformance pool) {
        this.pool = pool;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public DirectedFlowObservation getOutput() {
        return output;
    }

    public void setOutput(DirectedFlowObservation output) {
        this.output = output;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getResourceUse();
    }

    public ResourceUse getResourceUse() {
        return resourceUse;
    }

    public ActivityObservation getActivity() {
        return activity;
    }

    public DirectedFlowObservation getInput() {
        return input;
    }

    public Set<ResourceUseMeasurement> getMeasurements() {
        return measurements;
    }

    public void setInput(DirectedFlowObservation input) {
        this.input = input;
    }

    public void setStatus(ValueFlowStatus status) {
        this.status = status;
    }

    public ValueFlowStatus getStatus() {
        return status;
    }
}
