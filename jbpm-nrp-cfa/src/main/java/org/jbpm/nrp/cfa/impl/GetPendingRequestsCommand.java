package org.jbpm.nrp.cfa.impl;

import java.util.Collections;
import java.util.List;

import org.jbpm.nrp.cfa.ConversationActSummary;
import org.jbpm.services.task.utils.ClassUtil;

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
