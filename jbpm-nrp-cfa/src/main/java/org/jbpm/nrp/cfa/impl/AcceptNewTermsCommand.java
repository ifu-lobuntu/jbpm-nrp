package org.jbpm.nrp.cfa.impl;

import org.jbpm.nrp.cfa.ConversationActKind;
import org.jbpm.nrp.cfa.ConversationForActionState;
import org.jbpm.nrp.cfa.InternalConversationForAction;

public class AcceptNewTermsCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private long conversationActId;

	private String comment;

	public AcceptNewTermsCommand(String userId, long conversationActId, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		ConversationActImpl previous = find(ConversationActImpl.class, conversationActId);
		ConversationActImpl accept = super.createResponseCopy(previous, ConversationActKind.ACCEPT_NEW_TERMS);
		accept.setResultingConversationState(ConversationForActionState.IN_PROGRESS);
		accept.setCommitted(true);
		accept.setResponsePending(false);
		accept.setComment(comment);
		persist(accept);
		acceptCommitment(accept);
		((InternalConversationForAction) previous.getConversationForAction()).setConversationState(ConversationForActionState.IN_PROGRESS);
//		getTaskInstanceService().claim(accept.getConversationForAction().getId(), previous.getOwner().getId());
		return null;
	}


}
