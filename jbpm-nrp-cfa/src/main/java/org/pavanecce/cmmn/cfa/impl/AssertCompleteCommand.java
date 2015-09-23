package org.pavanecce.cmmn.cfa.impl;

import java.util.Map;

import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForAction;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;

public class AssertCompleteCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = -3174779982271593408L;

	private long conversationActId;

	private String comment;

	private Map<String, Object> output;

	public AssertCompleteCommand(String userId, long conversationActId, Map<String, Object> output, String comment) {
		this.userId = userId;
		this.conversationActId = conversationActId;
		this.comment = comment;
		this.output= output;
	}

	@Override
	public Void execute() {
		ConversationActImpl previous = find(ConversationActImpl.class, conversationActId);
		ConversationActImpl response = super.createResponseCopy(previous, ConversationActKind.ASSERT);
		ConversationForAction cfa = previous.getConversationForAction();
		response.setAddressedTo(cfa.getPeopleAssignments().getTaskInitiator());
		response.setOutputContentId(ensureContentIdPresent(cfa,response.getOutputContentId(),  output));
		response.setResultingConversationState(ConversationForActionState.AWAITING_ACCEPTANCE);
		response.setComment(comment);
		persist(response);
		((InternalConversationForAction) previous.getConversationForAction()).setConversationState(ConversationForActionState.AWAITING_ACCEPTANCE);
		InternalTaskData itd = (InternalTaskData) cfa.getTaskData();
		itd.setOutputContentId(ensureContentIdPresent(cfa, itd.getOutputContentId(), output));
		return null;
	}

}
