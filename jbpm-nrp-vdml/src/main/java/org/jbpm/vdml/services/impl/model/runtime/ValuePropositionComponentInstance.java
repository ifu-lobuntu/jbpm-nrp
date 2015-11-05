package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.ValuePropositionComponent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("ValuePropositionComponentInstance")
public class ValuePropositionComponentInstance extends ValueElementInstance{
    @ManyToOne
    private ValuePropositionInstance valueProposition;

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
}
