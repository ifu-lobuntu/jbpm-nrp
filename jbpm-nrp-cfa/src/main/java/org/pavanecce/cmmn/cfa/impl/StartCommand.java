package org.pavanecce.cmmn.cfa.impl;

import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;

public class StartCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private long conversationActId;

	private String comment;

	public StartCommand(String userId, long conversationActId, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		ConversationActImpl previous = find(ConversationActImpl.class, conversationActId);
		ConversationActImpl response = super.createResponseCopy(previous,ConversationActKind.START);
		response.setResultingConversationState(ConversationForActionState.IN_PROGRESS);
		response.setResponsePending(false);
		response.setComment(comment);
		persist(response);
		((InternalConversationForAction) previous.getConversationForAction()).setConversationState(ConversationForActionState.IN_PROGRESS);
		getTaskInstanceService().start(previous.getConversationForAction().getId(), userId);
		return null;
	}

}
