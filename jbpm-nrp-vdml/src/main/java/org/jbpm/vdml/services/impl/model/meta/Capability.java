package org.jbpm.vdml.services.impl.model.meta;

import org.jbpm.vdml.services.impl.model.runtime.ExchangeConfiguration;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Capability implements MetaEntity, MeasurableElement {
    @Id
    private String uri;
    private String name;
    @ManyToOne(cascade = CascadeType.ALL)
    private ExchangeConfiguration exchangeConfiguration;

    @ManyToOne
    private Capability extendedCapability;
    @ManyToMany
    private Set<Measure> measures = new HashSet<Measure>();

    public Capability() {

    }

    public Capability(String uri) {
        this.uri = uri;
    }
    public ExchangeConfiguration getExchangeConfiguration() {
        return exchangeConfiguration;
    }

    public void setExchangeConfiguration(ExchangeConfiguration exchangeConfiguration) {
        this.exchangeConfiguration = exchangeConfiguration;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Capability getExtendedCapability() {
        return extendedCapability;
    }

    public void setExtendedCapability(Capability superCapability) {
        this.extendedCapability = superCapability;
    }

    public Set<Measure> getMeasures() {
        return measures;
    }
}
