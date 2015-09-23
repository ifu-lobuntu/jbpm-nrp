package org.jbpm.vdml.services.api.model;


import java.io.Serializable;

public class LinkedExternalObject implements Serializable{
    private String metaEntityUri;
    private String objectType;
    private String identifier;

    public LinkedExternalObject() {
    }

    public LinkedExternalObject(String metaEntityUri, String objectType, String identifier) {
        this.metaEntityUri = metaEntityUri;
        this.objectType = objectType;
        this.identifier = identifier;
    }

    public String getMetaEntityUri() {
        return metaEntityUri;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getIdentifier() {
        return identifier;
    }
}
