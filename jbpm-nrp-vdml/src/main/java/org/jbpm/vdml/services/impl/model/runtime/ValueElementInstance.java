package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValueElement;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
@DiscriminatorColumn(name="type",length = 40)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class ValueElementInstance implements RuntimeEntity {
    @ManyToOne
    private ValueElement valueElement;
    @ManyToMany
    @JoinTable(name = "value_element_instance_aggregated_from",
            joinColumns = {@JoinColumn(name = "aggregated_to")},
            inverseJoinColumns = {@JoinColumn(name = "aggregated_from")})
    private Set<ValueElementInstance> aggregatedFrom = new HashSet<ValueElementInstance>();
    @ManyToMany(mappedBy = "aggregatedFrom")
    private Set<ValueElementInstance> aggregatedTo = new HashSet<ValueElementInstance>();
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public ValueElementInstance(ValueElement valueElement) {
        this.valueElement = valueElement;
    }

    public ValueElementInstance() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return valueElement;
    }

    public ValueElement getValueElement() {
        return valueElement;
    }

    public Set<ValueElementInstance> getAggregatedFrom() {
        return aggregatedFrom;
    }

    public Set<ValueElementInstance> getAggregatedTo() {
        return aggregatedTo;
    }

    public abstract Collection<? extends Measurement> getMeasurements();
}
