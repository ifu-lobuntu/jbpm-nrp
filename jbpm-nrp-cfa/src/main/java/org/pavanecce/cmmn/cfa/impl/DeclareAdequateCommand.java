package org.pavanecce.cmmn.cfa.impl;

import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;

public class DeclareAdequateCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private long conversationActId;

	private String comment;

	public DeclareAdequateCommand(String userId, long conversationActId, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		ConversationActImpl previous = find(ConversationActImpl.class, conversationActId);
		ConversationActImpl response = super.createResponseCopy(previous,ConversationActKind.DECLARE_ADEQUATE);
		response.setResultingConversationState(ConversationForActionState.CONSUMMATED);
		response.setComment(comment);
		InternalConversationForAction cfa = (InternalConversationForAction) previous.getConversationForAction();
		cfa.setOutcome(response);
		persist(response);
		taskId=cfa.getId();
		((InternalConversationForAction) previous.getConversationForAction()).setConversationState(ConversationForActionState.CONSUMMATED);
		getTaskInstanceService().complete(cfa.getId(), previous.getOwner().getId(), getContentAsMap(response.getOutputContentId()));
		return null;
	}
}
