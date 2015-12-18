package org.jbpm.nrp.cfa;

import org.kie.internal.task.api.model.InternalTask;

public interface InternalConversationForAction extends ConversationForAction, InternalTask {
	void setRequest(ConversationAct request);

	void setCommitment(ConversationAct commitment);

	void setOutcome(ConversationAct outcome);

	void setCurrentAct(ConversationAct result);

	void setConversationState(ConversationForActionState s);

}
