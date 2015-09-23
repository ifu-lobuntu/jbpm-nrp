package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Capability;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class CapabilityPerformance implements ActivatableRuntimeEntity,DirectlyExchangable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private CapabilityPerformance extendedCapabilityPerformance;
    @ManyToOne
    private Participant participant;
    @ManyToOne
    private Capability capability;
    @OneToMany()
    private Set<CapabilityMeasurement> measurements=new HashSet<CapabilityMeasurement>();//Aggregated from ActivityObservation.measurements

    public CapabilityPerformance() {
    }

    public CapabilityPerformance(Capability capability,Participant participant) {
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

    public CapabilityPerformance getExtendedCapabilityPerformance() {
        return extendedCapabilityPerformance;
    }

    public void setExtendedCapabilityPerformance(CapabilityPerformance extendedCapabilityPerformance) {
        this.extendedCapabilityPerformance = extendedCapabilityPerformance;
    }
    @Override
    public Participant getSupplier() {
        return getParticipant();
    }

    @Override
    public ExchangeConfiguration getExchangeConfiguration() {
        return capability.getExchangeConfiguration();
    }
}
