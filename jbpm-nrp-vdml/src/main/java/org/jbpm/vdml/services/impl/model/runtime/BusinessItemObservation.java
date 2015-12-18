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
    @OneToMany(mappedBy = "businessItem", cascade = CascadeType.ALL)
    private Set<BusinessItemMeasurement> measurements = new HashSet<BusinessItemMeasurement>();
    @OneToMany(mappedBy="deliverable")
    private Set<DeliverableFlowInstance> deliverableFlows = new HashSet<DeliverableFlowInstance>();
    @ManyToOne
    private ReusableBusinessItemPerformance sharedReference;

    @Embedded
    private ExternalObjectReference localReference;
    public BusinessItemObservation() {
    }

    public BusinessItemObservation(BusinessItemDefinition definition, CollaborationInstance collaboration) {
        this.definition = definition;
        this.collaboration = collaboration;
        this.collaboration.getBusinessItems().add(this);
    }

    public Set<DeliverableFlowInstance> getDeliverableFlows() {
        return deliverableFlows;
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

    public ReusableBusinessItemPerformance getSharedReference() {
        return sharedReference;
    }

    public void setSharedReference(ReusableBusinessItemPerformance sharedReference) {
        this.sharedReference = sharedReference;
    }
    public BusinessItemMeasurement findMeasurement(Measure m){
        return findMatchingRuntimeEntity(getMeasurements(),m);
    }

    public Set<BusinessItemMeasurement> getMeasurements() {
        return measurements;
    }

    public ExternalObjectReference getLocalReference() {
        return localReference;
    }

    public void setLocalReference(ExternalObjectReference localReference) {
        this.localReference = localReference;
    }
}
