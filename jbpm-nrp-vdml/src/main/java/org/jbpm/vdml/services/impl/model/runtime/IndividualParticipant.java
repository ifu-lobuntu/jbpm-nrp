package org.jbpm.vdml.services.impl.model.runtime;


import org.jbpm.vdml.services.impl.model.meta.BusinessItemDefinition;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
public class IndividualParticipant extends Participant{
    private String userName;
    @OneToMany(mappedBy = "represents", cascade = CascadeType.ALL)
    private Set<ReusableBusinessItemPerformance> representedActors =new HashSet<ReusableBusinessItemPerformance>();

    public IndividualParticipant(String userName) {
        this.userName = userName;
    }

    public IndividualParticipant() {
    }

    public Set<ReusableBusinessItemPerformance> getRepresentedActors() {
        return representedActors;
    }

    public String getUserName() {
        return userName;
    }

    public Collection<? extends BusinessItemDefinition> getRepresentedActorDefinitions() {
        Set<BusinessItemDefinition> result = new HashSet<BusinessItemDefinition>();
        for (ReusableBusinessItemPerformance representedActor : representedActors) {
            result.add(representedActor.getDefinition());
        }
        return result;
    }

}


