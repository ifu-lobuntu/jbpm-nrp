package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ValueProposition implements  MetaEntity{
    @Id
    private String uri;
    private String name;
    @ManyToOne
    private Role fromRole;
    @ManyToOne
    private Role toRole;
    @OneToMany(mappedBy = "valueProposition", cascade = CascadeType.ALL)
    private Set<ValuePropositionComponent> components=new HashSet<ValuePropositionComponent>();

    public ValueProposition() {
    }

    public ValueProposition(String uri, Role fromRole, Role toRole) {
        this.uri = uri;
        this.fromRole = fromRole;
        this.fromRole.getProvidedValuePropositions().add(this);
        this.toRole = toRole;
        this.toRole.getReceivedValuePropositions().add(this);
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

    public Role getFromRole() {
        return fromRole;
    }

    public Role getToRole() {
        return toRole;
    }

    public Set<ValuePropositionComponent> getComponents() {
        return components;
    }
}
