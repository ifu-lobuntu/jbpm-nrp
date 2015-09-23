package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.MetaEntity;

public interface RuntimeEntity {
    Long getId();
    MetaEntity getMetaEntity();
}
