package org.jbpm.vdml.services.impl.model.runtime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.FIELD)
public class ExternalObjectReference {
    private String objectType;
    private String identifier;

    public ExternalObjectReference(String objectType, String identifier) {
        this.objectType = objectType;
        this.identifier = identifier;
    }

    public ExternalObjectReference() {
    }

    public String getObjectType() {
        return objectType;
    }

    public String getIdentifier() {
        return identifier;
    }

}
