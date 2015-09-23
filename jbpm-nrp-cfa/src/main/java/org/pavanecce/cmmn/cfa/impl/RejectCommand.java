package org.pavanecce.cmmn.cfa.impl;

import java.util.Map;

import org.kie.api.task.model.Status;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;

public class RejectCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = 110312431L;

	private long conversationActId;

	private Map<String, Object> fault;

	private String comment;

	public RejectCommand(String userId, long conversationActId, Map<String, Object> fault, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.fault = fault;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		ConversationActImpl ca = super.find(ConversationActImpl.class, conversationActId);
		InternalConversationForAction cfa = (InternalConversationForAction) ca.getConversationForAction();
		if(!cfa.wasRequestedDirectly()){
			throw new IllegalArgumentException("Only requests directed directly at an owner can be rejected");
		}else if(!cfa.getRequest().getOwner().getId().equals(userId)){
			throw new IllegalArgumentException("Only '" + cfa.getRequest().getOwner().getId() +"' is allowed to reject this request");
		}
		ConversationActImpl rejection = super.createResponseCopy(ca, ConversationActKind.REJECT);
		rejection.setFaultContentId(ensureContentIdPresent(cfa, 0, fault));
		rejection.setComment(comment);
		rejection.setResultingConversationState(ConversationForActionState.NEGOTIATION_FAILED);
		persist(rejection);
		super.getTaskEventSupport().fireBeforeTaskFailed(cfa, taskContext);
		InternalTaskData itd = (InternalTaskData) cfa.getTaskData();
		itd.setFaultContentId(rejection.getFaultContentId());
		itd.setPreviousStatus(itd.getStatus());
		itd.setStatus(Status.Failed);
		cfa.setCurrentAct(rejection);
		cfa.setOutcome(rejection);
		super.getTaskEventSupport().fireAfterTaskFailed(cfa, taskContext);
		return null;
	}

}
