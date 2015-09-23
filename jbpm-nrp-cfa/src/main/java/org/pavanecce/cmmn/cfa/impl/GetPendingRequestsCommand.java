package org.pavanecce.cmmn.cfa.impl;

import java.util.Collections;
import java.util.List;

import org.jbpm.services.task.utils.ClassUtil;
import org.pavanecce.cmmn.cfa.api.ConversationActSummary;

public class GetPendingRequestsCommand extends AbstractConversationForActionCommand<List<ConversationActSummary>> {

	private static final long serialVersionUID = 11231231414L;

	public GetPendingRequestsCommand(String userId) {
		super();
		this.userId = userId;
	}

	@Override
	public List<ConversationActSummary> execute() {
		return super.taskPersistenceContext.queryWithParametersInTransaction("ConversationForActionGetPendingRequests",
				Collections.singletonMap("userId", (Object) userId), ClassUtil.<List<ConversationActSummary>> castClass(List.class));
	}

}
