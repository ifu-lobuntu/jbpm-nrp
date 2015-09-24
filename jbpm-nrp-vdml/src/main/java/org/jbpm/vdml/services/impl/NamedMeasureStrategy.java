package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.impl.model.runtime.RuntimeEntity;

public interface NamedMeasureStrategy {
    Object applyMeasurement(RuntimeEntity entity);
}
