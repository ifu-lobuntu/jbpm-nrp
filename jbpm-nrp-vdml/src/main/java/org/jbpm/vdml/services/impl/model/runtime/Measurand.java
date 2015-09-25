package org.jbpm.vdml.services.impl.model.runtime;


import java.util.Collection;

public interface Measurand {
    Collection<? extends Measurement> getMeasurements();
}
