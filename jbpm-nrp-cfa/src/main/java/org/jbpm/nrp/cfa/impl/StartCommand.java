package org.jbpm.nrp.cfa.impl;

import org.jbpm.nrp.cfa.ConversationActKind;
import org.jbpm.nrp.cfa.ConversationForActionState;
import org.jbpm.nrp.cfa.InternalConversationForAction;

public class StartCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private long conversationActId;

	private String comment;

	public  StartCommand(String userId, long conversationActId, String comment) {
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
