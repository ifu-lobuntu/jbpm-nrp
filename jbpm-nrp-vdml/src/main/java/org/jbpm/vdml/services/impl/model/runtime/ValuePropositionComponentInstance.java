package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
@DiscriminatorValue("ValuePropositionComponentInstance")
public class ValuePropositionComponentInstance extends ValueElementInstance{
    @ManyToOne
    private ValuePropositionInstance valueProposition;
    @OneToMany(mappedBy = "valuePropositionComponent",cascade = CascadeType.ALL)
    private Set<ValuePropositionComponentInstanceMeasurement> measurements=new HashSet<ValuePropositionComponentInstanceMeasurement>();

    public ValuePropositionComponentInstance() {
    }

    public ValuePropositionComponentInstance(ValuePropositionComponent valuePropositionComponent, ValuePropositionInstance valueProposition) {
        super(valuePropositionComponent);
        this.valueProposition=valueProposition;
        this.valueProposition.getComponents().add(this);
    }
    public void removeAllValueAddsAggregatedFrom() {
        for (ValueElementInstance v : new HashSet<ValueElementInstance>(getAggregatedFrom())) {
            if(v instanceof ValueAddInstance) {
                v.getAggregatedTo().remove(this);
                getAggregatedFrom().remove(v);
            }
        }
    }

    public ValuePropositionInstance getValueProposition() {
        return valueProposition;
    }

    public ValuePropositionComponent getValuePropositionComponent() {
        return (ValuePropositionComponent) super.getValueElement();
    }

    public void addValueAddsAggregatedFrom(Set<ValueAddInstance> vais) {
        for (ValueAddInstance vad : vais) {
            getAggregatedFrom().add(vad);
            vad.getAggregatedTo().add(this);
        }
    }

    public ValuePropositionComponentInstanceMeasurement getValueMeasurement(){
        return findMatchingRuntimeEntity(getMeasurements(),getValueElement().getValueMeasure());
    }

    public Set<ValuePropositionComponentInstanceMeasurement> getMeasurements() {
        return measurements;
    }
}
