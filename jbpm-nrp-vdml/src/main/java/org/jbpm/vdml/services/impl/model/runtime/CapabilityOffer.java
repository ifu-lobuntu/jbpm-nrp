package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Capability;
import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class CapabilityOffer implements ActivatableRuntimeEntity,DirectlyExchangable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private CapabilityOffer extendedCapabilityOffer;
    @ManyToOne
    private Participant participant;
    @ManyToOne
    private Capability capability;
    @ManyToMany
    @JoinTable(name="capability_resource")
    private Set<ReusableBusinessItemPerformance> capabilityResource=new HashSet<ReusableBusinessItemPerformance>();
    @OneToMany()
    private Set<CapabilityMeasurement> measurements=new HashSet<CapabilityMeasurement>();//Aggregated from ActivityInstance.measurements

    public CapabilityOffer() {
    }

    public CapabilityOffer(Capability capability, Participant participant) {
        this.participant = participant;
        this.participant.getCapabilityOffers().add(this);
        this.capability = capability;
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
        return getCapability();
    }

    public Participant getParticipant() {
        return participant;
    }

    public Capability getCapability() {
        return capability;
    }

    public Set< CapabilityMeasurement> getMeasurements() {
        return measurements;
    }

    public CapabilityOffer getExtendedCapabilityOffer() {
        return extendedCapabilityOffer;
    }

    public void setExtendedCapabilityOffer(CapabilityOffer extendedCapabilityOffer) {
        this.extendedCapabilityOffer = extendedCapabilityOffer;
    }
    @Override
    public Participant getSupplier() {
        return getParticipant();
    }

    @Override
    public ExchangeConfiguration getExchangeConfiguration() {
        return capability.getExchangeConfiguration();
    }

    public CapabilityMeasurement findMeasurement(Measure measure) {
        return findMatchingRuntimeEntity(getMeasurements(), measure);
    }
}
