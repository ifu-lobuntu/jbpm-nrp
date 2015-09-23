package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.Entity;

@Entity
public class PoolDefinition extends StoreDefinition {

    public PoolDefinition() {

    }

    public PoolDefinition(String uri) {
        super(uri);
    }
}
