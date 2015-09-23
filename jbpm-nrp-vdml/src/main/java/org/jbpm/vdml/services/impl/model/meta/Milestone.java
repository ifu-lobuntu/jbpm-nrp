package org.jbpm.vdml.services.impl.model.meta;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class Milestone implements MetaEntity{
    @Id
    private String uri;

    private String name;

    @ManyToOne
    private Collaboration collaboration;

    public Milestone() {
    }

    public Milestone(String uri, Collaboration collaboration) {
        this.uri = uri;
        this.collaboration = collaboration;
        this.collaboration.getMilestones().add(this);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getName() {
        return name;
    }

    public Collaboration getCollaboration() {
        return collaboration;
    }
}
