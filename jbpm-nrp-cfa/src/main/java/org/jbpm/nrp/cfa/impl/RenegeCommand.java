package org.jbpm.nrp.cfa.impl;

import java.util.Map;

import org.kie.api.task.model.Status;
import org.kie.internal.task.api.model.InternalTaskData;
import org.jbpm.nrp.cfa.ConversationActKind;
import org.jbpm.nrp.cfa.ConversationForActionState;
import org.jbpm.nrp.cfa.InternalConversationAct;
import org.jbpm.nrp.cfa.InternalConversationForAction;

public class RenegeCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = 110312431L;

	private long conversationActId;

	private Map<String, Object> fault;

	private String comment;

	public RenegeCommand(String userId, long conversationActId, Map<String, Object> fault, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.fault = fault;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		InternalConversationAct ca = super.find(ConversationActImpl.class, conversationActId);
		InternalConversationForAction cfa = (InternalConversationForAction) ca.getConversationForAction();
		ConversationActImpl renege = super.createResponseCopy(ca, ConversationActKind.RENEGE);
		renege.setFaultContentId(ensureContentIdPresent(cfa, 0, fault));
		renege.setComment(comment);
		renege.setResultingConversationState(ConversationForActionState.RENEGUED);
		renege.setDispute(true);
		renege.setResponsePending(false);
		persist(renege);
		super.getTaskEventSupport().fireBeforeTaskFailed(cfa, taskContext);
		InternalTaskData itd = (InternalTaskData) cfa.getTaskData();
		itd.setFaultContentId(renege.getFaultContentId());
		itd.setPreviousStatus(itd.getStatus());
		itd.setStatus(Status.Failed);
		cfa.setCurrentAct(renege);
		cfa.setOutcome(renege);
		super.getTaskEventSupport().fireAfterTaskFailed(cfa, taskContext);
		return null;
	}

}
