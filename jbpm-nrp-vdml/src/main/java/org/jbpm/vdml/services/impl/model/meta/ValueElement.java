package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;
import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByType;

@Entity
@DiscriminatorColumn(name="type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ValueElement implements MetaEntity, MeasurableElement {
    @Id
    protected String uri;
    private String name;
    @ManyToMany
    private Set<Measure> measures = new HashSet<Measure>();
    @ManyToMany
    @JoinTable(name = "value_element_aggregated_from",
            joinColumns = {@JoinColumn(name = "aggregated_to")},
            inverseJoinColumns = {@JoinColumn(name = "aggregated_from")})
    private Set<ValueElement> aggregatedFrom = new HashSet<ValueElement>();
    @ManyToMany(mappedBy = "aggregatedFrom")
    private Set<ValueElement> aggregatedTo = new HashSet<ValueElement>();

    @ManyToOne
    private Measure valueMeasure;
    public ValueElement(String uri) {
        this.uri = uri;
    }

    protected ValueElement() {
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Set<Measure> getMeasures() {
        return measures;
    }

    public Measure getValueMeasure() {
        return valueMeasure;
    }

    public void setValueMeasure(Measure valueMeasure) {
        this.valueMeasure = valueMeasure;
    }

    public Measure findMeasure(String name) {
        return findByName(getMeasures(), name);
    }

    public Set<ValueElement> getAggregatedFrom() {
        return aggregatedFrom;
    }

    public Set<ValueElement> getAggregatedTo() {
        return aggregatedTo;
    }

    public Set<ValueAdd> getValueAddsAggregatedFrom() {
        return findByType(getAggregatedFrom(), ValueAdd.class);
    }
}
