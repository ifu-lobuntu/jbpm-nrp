package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.EnumeratedMeasure;
import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;

import javax.persistence.*;

@MappedSuperclass
public abstract class Measurement implements ActivatableRuntimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Double actualValue;//need a double for aggregate operations
    private String actualRating;
    private Double plannedValue;//need a double for aggregate operations
    private String plannedRating;
    private boolean active;
    @ManyToOne
    private Measure measure;

    public Measurement() {
    }

    public Measurement(Measure measure) {
        this.measure = measure;
    }

    public Measure getMeasure() {
        return measure;
    }

    public Double getActualValue() {
        return actualValue;
    }

    public void setActualValue(Double actualValue) {
        this.actualValue = actualValue;
    }

    public Double getPlannedValue() {
        return plannedValue;
    }

    public void setPlannedValue(Double plannedValue) {
        this.plannedValue = plannedValue;
    }

    public Enum<?> getActualRating() {
        return resolveRating(this.actualRating);
    }
    public Enum<?> getPlannedRating() {
        return resolveRating(this.plannedRating);
    }

    private Enum<?> resolveRating(String actualRating) {
        if (getMeasure() instanceof EnumeratedMeasure) {
            Class enumClass = ((EnumeratedMeasure) getMeasure()).getEnumClass();
            return Enum.valueOf(enumClass, actualRating);
        }
        return null;
    }

    public void setActualRating(Enum<?> rating) {
        this.actualRating = rating.name();
    }
    public void setPlannedRating(Enum<?> rating) {
        this.plannedRating = rating.name();
    }

    public Long getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getMeasure();
    }

    public abstract RuntimeEntity getMeasurand();


}
