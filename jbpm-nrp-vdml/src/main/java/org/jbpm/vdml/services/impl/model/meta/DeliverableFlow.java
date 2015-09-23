package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;

@Entity
@DiscriminatorValue("DeliverableFlow")
public class DeliverableFlow extends DirectedFlow{
    @ManyToOne
    private Milestone milestone;

    public DeliverableFlow() {
    }

    public DeliverableFlow(String uri, Collaboration owningCollaboration) {
        super(uri, owningCollaboration);
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public Milestone getMilestone() {
        return milestone;
    }
}
