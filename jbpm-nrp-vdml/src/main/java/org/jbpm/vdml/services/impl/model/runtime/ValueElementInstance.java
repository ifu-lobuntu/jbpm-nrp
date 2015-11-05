package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValueElement;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorColumn(name="type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ValueElementInstance implements RuntimeEntity {
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
    @OneToMany(mappedBy = "valueElement",cascade = CascadeType.ALL)
    private Set<ValueElementInstanceMeasurement> measurements=new HashSet<ValueElementInstanceMeasurement>();
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

    public Set<ValueElementInstanceMeasurement> getMeasurements() {
        return measurements;
    }
}
