package org.pavanecce.cmmn.cfa.impl;

import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForAction;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;

public class PromiseCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private long conversationActId;

	private String comment;

	public PromiseCommand(String userId, long conversationActId, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		ConversationActImpl previous = find(ConversationActImpl.class, conversationActId);
		ConversationActImpl response = super.createResponseCopy(previous,ConversationActKind.PROMISE);
		response.setResultingConversationState(ConversationForActionState.PROMISED);
		response.setComment(comment);
		ConversationForAction conversationForAction = previous.getConversationForAction();
		persist(response);
		return null;
	}

}
