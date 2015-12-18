package org.jbpm.nrp.cfa.impl;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.*;

import org.jbpm.services.task.impl.model.*;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTaskData;
import org.jbpm.nrp.cfa.ConversationActKind;
import org.jbpm.nrp.cfa.ConversationForAction;
import org.jbpm.nrp.cfa.ConversationForActionState;
import org.jbpm.nrp.cfa.InternalConversationAct;

@Entity(name = "ConversationActImpl")
@DiscriminatorValue("ConversationAct")
public class ConversationActImpl extends TaskImpl implements InternalConversationAct {


    @ManyToOne
    private ConversationForActionImpl conversationForAction;

    @ManyToOne
    private ConversationActImpl previousStep;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfCommencement;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfCompletion;

    @Enumerated(EnumType.STRING)
    private ConversationActKind kind;

    @ManyToOne
    private OrganizationalEntityImpl addressedTo;

    @ManyToOne
    private UserImpl actor;

    @Basic
    boolean isResponsePending;

    @Basic
    boolean isDispute;

    @Enumerated
    private ConversationForActionState resultingConversationState;

    @Basic()
    @Column(name = "is_committed")
    private boolean isCommitted;


    @ManyToOne
    private OrganizationalEntityImpl owner;

    @ManyToOne
    private OrganizationalEntityImpl renegotiator;

    private String comment;

    public ConversationActImpl() {
        super.setPeopleAssignments(new PeopleAssignmentsImpl());
        super.setTaskData(new TaskDataImpl());
    }

    @Override
    public boolean isCommitted() {
        return isCommitted;
    }

    public void setCommitted(boolean isCommitted) {
        this.isCommitted = isCommitted;
    }

    @Override
    public String getFaultName() {
        return getTaskData().getFaultName();
    }

    public void setFaultName(String faultName) {
        ((InternalTaskData) getTaskData()).setFaultName(faultName);
    }

    @Override
    public String getFaultType() {
        return getTaskData().getFaultType();
    }

    public void setFaultType(String faultType) {
        ((InternalTaskData) getTaskData()).setFaultType(faultType);
    }

    @Override
    public OrganizationalEntity getRenegotiator() {
        return renegotiator;
    }

    public void setRenegotiator(OrganizationalEntity renegotiator) {
        this.renegotiator = (OrganizationalEntityImpl) renegotiator;
    }

    @Override
    public OrganizationalEntityImpl getOwner() {
        return owner;
    }

    public void setOwner(OrganizationalEntity owner) {
        this.owner = (OrganizationalEntityImpl) owner;
    }

    public ConversationActKind getKind() {
        return kind;
    }

    public ConversationForActionState getResultingConversationState() {
        return resultingConversationState;
    }

    public ConversationForAction getConversationForAction() {
        return conversationForAction;
    }

    public void setResultingConversationState(ConversationForActionState resultingConversationState) {
        this.resultingConversationState = resultingConversationState;
    }

    @Override
    public Date getDateOfCommencement() {
        return dateOfCommencement;
    }

    public void setDateOfCommencement(Date dateOfCommencement) {
        this.dateOfCommencement = dateOfCommencement;
    }

    @Override
    public Date getDateOfCompletion() {
        return dateOfCompletion;
    }

    public void setDateOfCompletion(Date dateOfCompletion) {
        this.dateOfCompletion = dateOfCompletion;
    }

    public void setConversationForAction(ConversationForActionImpl conversationForAction) {
        this.conversationForAction = conversationForAction;
    }

    public void setKind(ConversationActKind kind) {
        this.kind = kind;
    }

    public ConversationActImpl getPreviousStep() {
        return previousStep;
    }

    public void setPreviousStep(InternalConversationAct previousStep) {
        this.previousStep = (ConversationActImpl) previousStep;
    }

    @Override
    public OrganizationalEntityImpl getAddressedTo() {
        return addressedTo;
    }

    public void setAddressedTo(OrganizationalEntity user) {
        this.addressedTo = (OrganizationalEntityImpl) user;
        ((InternalPeopleAssignments) super.getPeopleAssignments()).setPotentialOwners(Arrays.asList(user));

    }

    @Override
    public UserImpl getActor() {
        return actor;
    }

    public void setActor(User user) {
        this.actor = (UserImpl) user;
        ((InternalPeopleAssignments) super.getPeopleAssignments()).setTaskInitiator(user);
    }

    public boolean isResponsePending() {
        return isResponsePending;
    }

    public void setResponsePending(boolean responsePending) {
        this.isResponsePending = responsePending;
    }

    @Override
    public long getInputContentId() {
        return super.getTaskData().getDocumentContentId();
    }

    public void setInputContentId(long inputContentId) {
        ((InternalTaskData) getTaskData()).setDocumentContentId(inputContentId);

    }

    @Override
    public long getOutputContentId() {
        return super.getTaskData().getOutputContentId();
    }

    public void setOutputContentId(long outputContentId) {
        ((InternalTaskData) getTaskData()).setOutputContentId(outputContentId);
    }

    @Override
    public long getFaultContentId() {
        return super.getTaskData().getFaultContentId();
    }

    public void setFaultContentId(long faultContentId) {
        ((InternalTaskData) getTaskData()).setFaultContentId(faultContentId);
    }

    public void setConversationForAction(ConversationForAction cfa) {
        this.conversationForAction = (ConversationForActionImpl) cfa;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public boolean isDispute() {
        return isDispute;
    }

    public void setDispute(boolean isDispute) {
        this.isDispute = isDispute;
    }

}
