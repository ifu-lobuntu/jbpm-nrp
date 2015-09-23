package org.jbpm.vdml.services.impl.model.runtime;


public interface ActivatableRuntimeEntity extends RuntimeEntity{
    boolean isActive();
    void setActive(boolean a);
}
