package org.pavanecce.cmmn.cfa.api;

import org.kie.api.task.model.Task;

public interface ConversationForAction extends Task{
	ConversationAct getRequest();
	ConversationAct getCommitment();
	ConversationAct getOutcome();
	ConversationAct getCurrentAct();
	boolean wasRequestedDirectly();
	
}
