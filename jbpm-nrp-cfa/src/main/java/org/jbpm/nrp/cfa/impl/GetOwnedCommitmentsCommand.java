package org.jbpm.nrp.cfa.impl;

import java.util.Collections;
import java.util.List;

import org.jbpm.services.task.utils.ClassUtil;
import org.jbpm.nrp.cfa.ConversationForActionSummary;

public class GetOwnedCommitmentsCommand extends AbstractConversationForActionCommand<List<ConversationForActionSummary>> {

	private static final long serialVersionUID = 11231231414L;

	public GetOwnedCommitmentsCommand(String userId) {
		super();
		this.userId = userId;
	}

	@Override
	public List<ConversationForActionSummary> execute() {
		return super.taskPersistenceContext.queryWithParametersInTransaction("ConversationForActionGetOwnedCommitments",
				Collections.singletonMap("userId", (Object) userId), ClassUtil.<List<ConversationForActionSummary>> castClass(List.class));
	}

}
