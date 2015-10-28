package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ResourceUse implements MetaEntity{
    @Id
    private String uri;
    @ManyToOne
    private Activity activity;
    @ManyToOne
    private InputPort input;
    @ManyToOne
    private OutputPort output;
    @ManyToOne
    private Measure quantity;
    @ManyToOne
    private Measure duration;
    @Enumerated
    private ResourceUseLocation resourceUseLocation;

    private String name;

    public ResourceUse() {
    }

    public ResourceUse(String uri, Activity activity) {
        this.uri = uri;
        this.activity = activity;
        this.activity.getResourceUses().add(this);
    }


    @Override
    public String getUri() {
        return uri;
    }

    public Activity getActivity() {
        return activity;
    }

    public InputPort getInput() {
        return input;
    }

    public void setInput(InputPort input) {
        this.input = input;
    }

    public OutputPort getOutput() {
        return output;
    }

    public void setOutput(OutputPort output) {
        this.output = output;
    }

    public Measure getQuantity() {
        return quantity;
    }

    public void setQuantity(Measure quantity) {
        this.quantity = quantity;
    }

    public Measure getDuration() {
        return duration;
    }

    public void setDuration(Measure duration) {
        this.duration = duration;
    }

    public ResourceUseLocation getResourceUseLocation() {
        return resourceUseLocation;
    }

    public void setResourceUseLocation(ResourceUseLocation resourceUseLocation) {
        this.resourceUseLocation = resourceUseLocation;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
