package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.ValueNetwork;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ValueNetworkInstance extends Participant implements RuntimeEntity {

    @ManyToOne
    private ValueNetwork valueNetwork;


    public ValueNetworkInstance(ValueNetwork collaboration) {
        this.valueNetwork = collaboration;
    }

    public ValueNetworkInstance() {
    }

    @Override
    public MetaEntity getMetaEntity() {
        return getValueNetwork();
    }


    public Collaboration getValueNetwork() {
        return valueNetwork;
    }


}
