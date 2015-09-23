package org.pavanecce.cmmn.cfa.impl;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import org.jbpm.services.task.impl.model.TaskImpl;
import org.pavanecce.cmmn.cfa.api.ConversationAct;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;

@Entity(name = "ConversationForActionImpl")
@DiscriminatorValue("ConversationForAction")
public class ConversationForActionImpl extends TaskImpl implements InternalConversationForAction {
	@OneToOne
	private ConversationActImpl request;

	@OneToOne
	private ConversationActImpl commitment;
	@OneToOne
	private ConversationActImpl currentAct;

	@OneToOne
	private ConversationActImpl outcome;

	@Enumerated(EnumType.STRING)
	private ConversationForActionState conversationState;
	@Override
	public ConversationAct getCurrentAct() {
		return currentAct;
	}
	public void setCurrentAct(ConversationAct currentAct) {
		this.currentAct = (ConversationActImpl) currentAct;
	}
	@Override
	public ConversationAct getRequest() {
		return request;
	}

	@Override
	public ConversationAct getCommitment() {
		return commitment;
	}

	@Override
	public ConversationAct getOutcome() {
		return outcome;
	}

	public ConversationForActionState getConversationState() {
		return conversationState;
	}

	public void setConversationState(ConversationForActionState state) {
		this.conversationState = state;
	}

	public void setRequest(ConversationAct  request) {
		this.request = (ConversationActImpl) request;
	}

	public void setCommitment(ConversationAct commitment) {
		this.commitment = (ConversationActImpl) commitment;
	}

	public void setOutcome(ConversationAct outcome) {
		this.outcome = (ConversationActImpl) outcome;
	}
	@Override
	public boolean wasRequestedDirectly() {
		return getRequest()!=null && getRequest().getOwner()!=null;
	}
	

}
