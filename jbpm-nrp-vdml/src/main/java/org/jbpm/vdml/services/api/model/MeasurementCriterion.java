package org.jbpm.vdml.services.api.model;


import java.io.Serializable;

public class MeasurementCriterion implements Serializable{
    private String measureUri;
    private Double lower;
    private Double upper;
    private CriterionOperator operator;

    public MeasurementCriterion(String measureUri,CriterionOperator operator, Double value) {
        this.measureUri=measureUri;
        this.operator = operator;
        if(operator==CriterionOperator.LESS_THAN) {
            this.upper= value;
        }else{
            this.lower = value;
        }
    }
    public MeasurementCriterion(String measureUri, Double lower, Double upper) {
        this.measureUri=measureUri;
        this.operator = CriterionOperator.BETWEEN;
        this.lower = lower;
        this.upper = upper;
    }

    public String getMeasureUri() {
        return measureUri;
    }

    public MeasurementCriterion() {
    }

    public Double getLower() {
        return lower;
    }

    public Double getUpper() {
        return upper;
    }

    public CriterionOperator getOperator() {
        return operator;
    }
}
