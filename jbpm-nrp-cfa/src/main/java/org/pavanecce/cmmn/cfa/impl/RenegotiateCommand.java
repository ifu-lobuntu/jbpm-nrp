package org.pavanecce.cmmn.cfa.impl;

import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForAction;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.NegotiationStep;

public class RenegotiateCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private NegotiationStep request;

	public RenegotiateCommand(String userId, NegotiationStep request) {
		this.userId = userId;
		this.request = request;
	}

	@Override
	public Void execute() {
		ConversationActImpl previous = find(ConversationActImpl.class, request.getPreviousActId());
		ConversationActImpl renegotiation = super.createResponseCopy(previous, ConversationActKind.RENEGOTIATE);
		renegotiation.setRenegotiator(renegotiation.getActor());
		if(renegotiation.getActor().getId().equals(renegotiation.getOwner().getId())){
			renegotiation.setAddressedTo(renegotiation.getConversationForAction().getPeopleAssignments().getTaskInitiator());
		}else{
			renegotiation.setAddressedTo(renegotiation.getOwner());
		}
		ConversationForAction cfa = previous.getConversationForAction();
		renegotiation.setDateOfCommencement(request.getDateOfCommencement());
		renegotiation.setDateOfCompletion(request.getDateOfCompletion());
		renegotiation.setInputContentId(ensureContentIdPresent(cfa, renegotiation.getOutputContentId(), request.getInput()));
		renegotiation.setOutputContentId(ensureContentIdPresent(cfa, renegotiation.getOutputContentId(), request.getOutput()));
		renegotiation.setResultingConversationState(ConversationForActionState.RENEGOTIATION_REQUESTED);
		renegotiation.setComment(request.getComment());
		persist(renegotiation);
		return null;
	}
}
