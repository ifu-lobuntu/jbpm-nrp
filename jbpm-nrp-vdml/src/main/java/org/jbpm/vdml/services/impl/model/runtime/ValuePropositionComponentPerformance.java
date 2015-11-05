package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
public class ValuePropositionComponentPerformance implements ActivatableRuntimeEntity, Measurand{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean active;
    @ManyToOne
    private ValuePropositionPerformance valueProposition;
    @ManyToOne
    private ValuePropositionComponent valuePropositionComponent;
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    private Set<ValuePropositionComponentPerformanceMeasurement> measurements=new HashSet<ValuePropositionComponentPerformanceMeasurement>();//Aggregated from related ValueAdds

    public ValuePropositionComponentPerformance() {
    }

    public ValuePropositionComponentPerformance(ValuePropositionComponent valuePropositionComponent, ValuePropositionPerformance valueProposition) {
        this.valueProposition = valueProposition;
        this.valuePropositionComponent = valuePropositionComponent;
        this.valueProposition.getComponents().add(this);
    }

    public ValuePropositionPerformance getValueProposition() {
        return valueProposition;
    }


    public ValuePropositionComponent getValuePropositionComponent() {
        return valuePropositionComponent;
    }


    public Set<ValuePropositionComponentPerformanceMeasurement> getMeasurements() {
        return this.measurements;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getValuePropositionComponent();
    }

    public ValuePropositionComponentPerformanceMeasurement findMeasurement(Measure measure) {
        ValuePropositionComponentPerformanceMeasurement result = findMatchingRuntimeEntity(getMeasurements(), measure);
        return result;
    }
}
