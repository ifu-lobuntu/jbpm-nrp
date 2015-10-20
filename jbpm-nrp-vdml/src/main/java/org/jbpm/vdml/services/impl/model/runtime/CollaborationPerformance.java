package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class CollaborationPerformance extends Participant implements RuntimeEntity {

    @ManyToOne
    private Collaboration collaboration;


    @ManyToMany()
    private Set<RolePerformance> rolePerformances = new HashSet<RolePerformance>();

    public CollaborationPerformance(Collaboration collaboration) {
        this.collaboration = collaboration;
    }

    public CollaborationPerformance() {
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getCollaboration();
    }


    public Collaboration getCollaboration() {
        return collaboration;
    }


    public Set<RolePerformance> getRolePerformances() {
        return rolePerformances;
    }

}
