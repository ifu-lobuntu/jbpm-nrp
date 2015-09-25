package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
public class ValueProposition implements  MetaEntity{
    @Id
    private String uri;
    private String name;
    @ManyToOne
    private Role provider;
    @ManyToOne
    private Role recipient;
    @OneToMany(mappedBy = "valueProposition", cascade = CascadeType.ALL)
    private Set<ValuePropositionComponent> components=new HashSet<ValuePropositionComponent>();
    @ManyToOne
    private Collaboration collaboration;

    public ValueProposition() {
    }

    public ValueProposition(String uri, Role provider, Role recipient) {
        this.uri = uri;
        this.provider = provider;
        this.provider.getProvidedValuePropositions().add(this);
        this.recipient = recipient;
        this.recipient.getReceivedValuePropositions().add(this);
        this.collaboration=provider.getCollaboration();
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Role getProvider() {
        return provider;
    }

    public Role getRecipient() {
        return recipient;
    }

    public Set<ValuePropositionComponent> getComponents() {
        return components;
    }

    public Collaboration getCollaboration() {
        return collaboration;
    }

    public ValuePropositionComponent findComponent(String name) {
        return findByName(getComponents(),name);
    }


}
