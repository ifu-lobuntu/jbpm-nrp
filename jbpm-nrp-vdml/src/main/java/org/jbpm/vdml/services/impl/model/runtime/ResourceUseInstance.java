package org.jbpm.vdml.services.impl.model.runtime;

import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ResourceUse;
import org.jbpm.vdml.services.impl.model.scheduling.PlannedUnavailability;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ResourceUseInstance implements RuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private ResourceUse resourceUse;
    @ManyToOne
    private ActivityInstance activity;
    @ManyToOne
    private DeliverableFlowInstance input;
    @ManyToOne
    private DeliverableFlowInstance output;
    @ManyToOne
    private Address address;

    @ManyToOne
    private PoolPerformance pool;
    @ManyToOne
    private ReusableBusinessItemPerformance reusableResource;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime plannedFromDateTime;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime actualFromDateTime;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime actualToDateTime;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime plannedToDateTime;
    @ManyToOne
    private PlannedUnavailability plannedUnavailability;

    private double quantity;


    @OneToMany(mappedBy = "resourceUse")
    private Set<ResourceUseMeasurement> measurements = new HashSet<ResourceUseMeasurement>();
    @Enumerated
    private ValueFlowStatus status;

    public ResourceUseInstance() {
    }
    public ResourceUseInstance(ResourceUse resourceUse, ActivityInstance activity) {
        this.resourceUse = resourceUse;
        this.activity = activity;
        this.activity.getResourceUseInstance().add(this);
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

    public DateTime getPlannedFromDateTime() {
        return plannedFromDateTime;
    }

    public void setPlannedFromDateTime(DateTime plannedFromDateTime) {
        this.plannedFromDateTime = plannedFromDateTime;
    }

    public DateTime getActualFromDateTime() {
        return actualFromDateTime;
    }

    public void setActualFromDateTime(DateTime actualFromDateTime) {
        this.actualFromDateTime = actualFromDateTime;
    }

    public DateTime getActualToDateTime() {
        return actualToDateTime;
    }

    public void setActualToDateTime(DateTime actualToDateTime) {
        this.actualToDateTime = actualToDateTime;
    }

    public DateTime getPlannedToDateTime() {
        return plannedToDateTime;
    }

    public void setPlannedToDateTime(DateTime plannedToDateTime) {
        this.plannedToDateTime = plannedToDateTime;
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

    public DeliverableFlowInstance getOutput() {
        return output;
    }

    public void setOutput(DeliverableFlowInstance output) {
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

    public ActivityInstance getActivity() {
        return activity;
    }

    public DeliverableFlowInstance getInput() {
        return input;
    }

    public Set<ResourceUseMeasurement> getMeasurements() {
        return measurements;
    }

    public void setInput(DeliverableFlowInstance input) {
        this.input = input;
    }

    public void setStatus(ValueFlowStatus status) {
        this.status = status;
    }

    public ValueFlowStatus getStatus() {
        return status;
    }
}
