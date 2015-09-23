package org.pavanecce.cmmn.cfa.impl;

import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForAction;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.NegotiationStep;

public class CounterToOwnerCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private NegotiationStep request;

	public CounterToOwnerCommand(String userId, NegotiationStep request) {
		this.userId = userId;
		this.request = request;
	}

	@Override
	public Void execute() {
		ConversationActImpl previous = find(ConversationActImpl.class, request.getPreviousActId());
		ConversationActImpl counter = super.createResponseCopy(previous, ConversationActKind.COUNTER_TO_OWNER);
		ConversationForAction cfa = previous.getConversationForAction();
		counter.setDateOfCommencement(request.getDateOfCommencement());
		counter.setDateOfCompletion(request.getDateOfCompletion());
		counter.setInputContentId(ensureContentIdPresent(cfa, counter.getOutputContentId(), request.getInput()));
		counter.setOutputContentId(ensureContentIdPresent(cfa, counter.getOutputContentId(), request.getOutput()));
		counter.setResultingConversationState(ConversationForActionState.REQUESTED);
		counter.setComment(request.getComment());
		persist(counter);
		return null;
	}
}
