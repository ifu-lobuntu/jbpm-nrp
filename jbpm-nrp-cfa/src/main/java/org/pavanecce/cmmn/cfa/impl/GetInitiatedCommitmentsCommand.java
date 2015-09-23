package org.pavanecce.cmmn.cfa.impl;

import java.util.Collections;
import java.util.List;

import org.jbpm.services.task.utils.ClassUtil;
import org.pavanecce.cmmn.cfa.api.ConversationForActionSummary;

public class GetInitiatedCommitmentsCommand extends AbstractConversationForActionCommand<List<ConversationForActionSummary>> {

	private static final long serialVersionUID = 11231231414L;

	public GetInitiatedCommitmentsCommand(String userId) {
		super();
		this.userId = userId;
	}

	@Override
	public List<ConversationForActionSummary> execute() {
		return super.taskPersistenceContext.queryWithParametersInTransaction("ConversationForActionGetInitiatedCommitments",
				Collections.singletonMap("userId", (Object) userId), ClassUtil.<List<ConversationForActionSummary>> castClass(List.class));
	}

}
