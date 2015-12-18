package org.jbpm.nrp.cfa.impl;

import org.jbpm.nrp.cfa.ConversationActKind;
import org.jbpm.nrp.cfa.ConversationForActionState;
import org.jbpm.nrp.cfa.InternalConversationForAction;

public class AcceptCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private long conversationActId;

	private String comment;

	public AcceptCommand(String userId, long conversationActId, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		ConversationActImpl previous = find(ConversationActImpl.class, conversationActId);
		ConversationActImpl accept = super.createResponseCopy(previous,ConversationActKind.ACCEPT);
		accept.setResultingConversationState(ConversationForActionState.COMMITTED);
		accept.setCommitted(true);
		accept.setComment(comment);
		persist(accept);
		acceptCommitment(accept);
		((InternalConversationForAction) previous.getConversationForAction()).setConversationState(ConversationForActionState.COMMITTED);
		getTaskInstanceService().claim(accept.getConversationForAction().getId(), previous.getOwner().getId());
		return null;
	}


}
