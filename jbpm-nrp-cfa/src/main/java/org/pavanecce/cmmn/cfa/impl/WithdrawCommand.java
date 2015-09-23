package org.pavanecce.cmmn.cfa.impl;

import java.util.Map;

import org.kie.api.task.model.Status;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForAction;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;

public class WithdrawCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = 110312431L;

	private long conversationActId;

	private Map<String, Object> fault;

	private String comment;

	public WithdrawCommand(String userId, long conversationActId, Map<String, Object> fault, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.fault = fault;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		ConversationActImpl ca = super.find(ConversationActImpl.class, conversationActId);
		InternalConversationForAction cfa = (InternalConversationForAction) ca.getConversationForAction();
		ConversationActImpl withdrawal = super.createResponseCopy(cfa.getCurrentAct().getId(), ConversationActKind.WITHDRAW);
		withdrawal.setFaultContentId(ensureContentIdPresent(cfa, 0, fault));
		withdrawal.setComment(comment);
		withdrawal.setResultingConversationState(ConversationForActionState.NEGOTIATION_FAILED);
		persist(withdrawal);
		super.getTaskEventSupport().fireBeforeTaskFailed(cfa, taskContext);
		InternalTaskData itd = (InternalTaskData) cfa.getTaskData();
		itd.setFaultContentId(withdrawal.getFaultContentId());
		itd.setPreviousStatus(itd.getStatus());
		itd.setStatus(Status.Failed);
		cfa.setCurrentAct(withdrawal);
		cfa.setOutcome(withdrawal);
		super.getTaskEventSupport().fireAfterTaskFailed(cfa, taskContext);
		return null;
	}

}
