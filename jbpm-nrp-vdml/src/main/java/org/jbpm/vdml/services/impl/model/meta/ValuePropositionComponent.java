package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.*;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
@DiscriminatorValue("ValuePropositionComponent")
public class ValuePropositionComponent extends ValueElement {
    @ManyToOne
    private ValueProposition valueProposition;
    @ManyToOne(cascade = CascadeType.ALL)
    private Measure percentageWeight;
    @ManyToOne(cascade = CascadeType.ALL)
    private Measure satisfactionLevel;

    public ValuePropositionComponent(String uri, ValueProposition valueProposition) {
        super(uri);
        this.valueProposition = valueProposition;
        this.valueProposition.getComponents().add(this);

    }

    public ValuePropositionComponent() {
    }

    public Measure getPercentageWeight() {
        return percentageWeight;
    }

    public void setPercentageWeight(Measure percentageWeight) {
        this.percentageWeight = percentageWeight;
    }

    public Measure getSatisfactionLevel() {
        return satisfactionLevel;
    }

    public void setSatisfactionLevel(Measure satisfactionLevel) {
        this.satisfactionLevel = satisfactionLevel;
    }

    public ValueProposition getValueProposition() {
        return valueProposition;
    }

    public Map<PortContainer, Collection<ValueAdd>> findValueProducers() {
        Map<PortContainer, Collection<ValueAdd>> valueProducers = new HashMap<PortContainer, Collection<ValueAdd>>();
        for (ValueElement ve : getAggregatedFrom()) {
            if (ve instanceof ValueAdd) {
                ValueAdd va = (ValueAdd) ve;
                Collection<ValueAdd> vas = valueProducers.get(va.getOutputPort().getPortContainer());
                if (vas == null) {
                    valueProducers.put(va.getOutputPort().getPortContainer(), vas = new HashSet<ValueAdd>());
                }
                vas.add(va);
            }
        }
        return valueProducers;
    }
}
