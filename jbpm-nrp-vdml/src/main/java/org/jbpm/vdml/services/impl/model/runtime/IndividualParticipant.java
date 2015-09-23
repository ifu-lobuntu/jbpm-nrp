package org.jbpm.vdml.services.impl.model.runtime;


import javax.persistence.Entity;

@Entity
public class IndividualParticipant extends Participant{
    private String userName;

    public IndividualParticipant(String userName) {
        this.userName = userName;
    }

    public IndividualParticipant() {
    }

    public String getUserName() {
        return userName;
    }
}


