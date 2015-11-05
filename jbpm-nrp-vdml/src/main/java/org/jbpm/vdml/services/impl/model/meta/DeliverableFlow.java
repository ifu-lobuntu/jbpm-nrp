package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
@DiscriminatorValue("DeliverableFlow")
public class DeliverableFlow extends DirectedFlow{
    @ManyToOne
    private Milestone milestone;

    public DeliverableFlow() {
    }
    public ValueAdd findValueAdd(String name) {
        return findByName(((OutputPort) getSource()).getValueAdds(), name);
    }

    @Override
    public OutputPort getSource() {
        return (OutputPort) super.getSource();
    }

    @Override
    public InputPort getTarget() {
        return (InputPort) super.getTarget();
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
