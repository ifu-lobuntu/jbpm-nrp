package org.jbpm.nrp.cfa.impl;

import java.util.Map;

public class GetContentMapCommand extends AbstractConversationForActionCommand<Map<String,Object>> {

	private static final long serialVersionUID = 11231231414L;
	private long contentId;

	public GetContentMapCommand(long conversationForActionId, long contentId) {
		super();
		this.contentId=contentId;
		super.taskId=conversationForActionId;
	}

	@Override
	public Map<String, Object> execute() {
		return getContentAsMap(contentId);
	}


}
