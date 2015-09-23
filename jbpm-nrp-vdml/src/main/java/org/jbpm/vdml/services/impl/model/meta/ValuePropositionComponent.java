package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ValuePropositionComponent implements MetaEntity,MeasurableElement{
    @Id
    private String uri;
    private String name;
    @ManyToOne
    private ValueProposition valueProposition;
    @ManyToMany
    private Set<Measure> measures = new HashSet<Measure>();
    public ValuePropositionComponent(String uri, ValueProposition valueProposition) {
        this.uri = uri;
        this.valueProposition = valueProposition;
        this.valueProposition.getComponents().add(this);
    }

    public ValuePropositionComponent() {
    }

    @Override
    public String getUri() {
        return uri;
    }

    public ValueProposition getValueProposition() {
        return valueProposition;
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
}
