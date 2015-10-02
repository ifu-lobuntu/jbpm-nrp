package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.BusinessItemDefinition;
import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.*;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class BusinessItemObservation implements RuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private BusinessItemDefinition definition;
    @ManyToOne
    private CollaborationInstance collaboration;
    @OneToMany(cascade = CascadeType.ALL)
    private Set<BusinessItemMeasurement> measurements = new HashSet<BusinessItemMeasurement>();
    @ManyToOne
    private ReusableBusinessItemPerformance instanceReference;

    public BusinessItemObservation() {
    }

    public BusinessItemObservation(BusinessItemDefinition definition, CollaborationInstance collaboration) {
        this.definition = definition;
        this.collaboration = collaboration;
        this.collaboration.getBusinessItems().add(this);
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

    public CollaborationInstance getCollaboration() {
        return collaboration;
    }

    public ReusableBusinessItemPerformance getInstanceReference() {
        return instanceReference;
    }

    public void setInstanceReference(ReusableBusinessItemPerformance instanceReference) {
        this.instanceReference = instanceReference;
    }
    public BusinessItemMeasurement findMeasurement(Measure m){
        return findMatchingRuntimeEntity(getMeasurements(),m);
    }

    public Set<BusinessItemMeasurement> getMeasurements() {
        return measurements;
    }
}
