package org.jbpm.nrp.common;


public interface IntervalEnum {
    public Double getMaximumEndpoint();
    public Boolean getMaximumOpen();
    public Double getMinimumEndpoint();
    public Boolean getMinimumOpen();

    String name();
}
