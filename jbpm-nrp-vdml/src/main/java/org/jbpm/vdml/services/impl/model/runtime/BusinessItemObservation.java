package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.BusinessItemDefinition;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class BusinessItemObservation implements RuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private BusinessItemDefinition businessItemDefinition;
    @ManyToOne
    private CollaborationObservation collaboration;
    @OneToMany(cascade = CascadeType.ALL)
    private Set<BusinessItemMeasurement> measurements = new HashSet<BusinessItemMeasurement>();
    @Embedded
    private ExternalObjectReference instanceReference=new ExternalObjectReference();

    public BusinessItemObservation() {
    }

    public BusinessItemObservation(BusinessItemDefinition businessItemDefinition, CollaborationObservation collaboration) {
        this.businessItemDefinition = businessItemDefinition;
        this.collaboration = collaboration;
        this.collaboration.getBusinessItems().add(this);
    }

    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getBusinessItemDefinition();
    }

    public BusinessItemDefinition getBusinessItemDefinition() {
        return businessItemDefinition;
    }

    public CollaborationObservation getCollaboration() {
        return collaboration;
    }

    public ExternalObjectReference getInstanceReference() {
        return instanceReference;
    }

    public void setInstanceReference(ExternalObjectReference instanceReference) {
        this.instanceReference = instanceReference;
    }

    public Set<BusinessItemMeasurement> getMeasurements() {
        return measurements;
    }
}
