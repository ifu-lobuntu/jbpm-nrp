package org.jbpm.nrp.cfa.impl;

import java.util.Map;

import org.kie.internal.task.api.model.InternalTaskData;
import org.jbpm.nrp.cfa.ConversationActKind;
import org.jbpm.nrp.cfa.ConversationForActionState;
import org.jbpm.nrp.cfa.InternalConversationForAction;

public class DeclareInadequateCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = 110312431L;

	private long conversationActId;

	private Map<String, Object> fault;

	private String comment;

	public DeclareInadequateCommand(String userId, long conversationActId, Map<String, Object> fault, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.fault = fault;
		this.comment = comment;
	}

	@Override
	public Void execute() {
		ConversationActImpl assertion = super.find(ConversationActImpl.class, conversationActId);
		InternalConversationForAction cfa = (InternalConversationForAction) assertion.getConversationForAction();
		ConversationActImpl declaration = super.createResponseCopy(assertion, ConversationActKind.DECLARE_INADEQUATE);
		declaration.setFaultContentId(ensureContentIdPresent(cfa, 0, fault));
		declaration.setComment(comment);
		declaration.setResultingConversationState(ConversationForActionState.IN_PROGRESS);
		persist(declaration);
		InternalTaskData itd = (InternalTaskData) cfa.getTaskData();
		itd.setFaultContentId(declaration.getFaultContentId());
		itd.setPreviousStatus(itd.getStatus());
		cfa.setCurrentAct(declaration);
		return null;
	}

}
