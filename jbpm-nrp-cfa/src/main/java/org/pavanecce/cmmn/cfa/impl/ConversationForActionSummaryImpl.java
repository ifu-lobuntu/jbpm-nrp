package org.pavanecce.cmmn.cfa.impl;

import java.util.Date;

import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.InternalTaskSummary;
import org.kie.internal.task.api.model.SubTasksStrategy;
import org.pavanecce.cmmn.cfa.api.ConversationForActionSummary;

public class ConversationForActionSummaryImpl extends TaskSummaryImpl implements ConversationForActionSummary {

	public ConversationForActionSummaryImpl() {
		super();
	}

	public ConversationForActionSummaryImpl(long id, String name, String subject, String description, Status status, int priority, boolean skipable, User actualOwner, User createdBy, Date createdOn, Date activationTime, Date expirationTime, String processId, long processSessionId, long processInstanceId, String deploymentId, SubTasksStrategy subTaskStrategy, long parentId) {
		super(id, name, subject, description, status, priority, skipable, actualOwner, createdBy, createdOn, activationTime, expirationTime, processId, processSessionId, processInstanceId, deploymentId, subTaskStrategy, parentId);
	}

	public ConversationForActionSummaryImpl(long id, String name, String subject, String description, Status status, int priority, boolean skipable, String actualOwnerId, String createdById, Date createdOn, Date activationTime, Date expirationTime, String processId, long processSessionId, long processInstanceId, String deploymentId, SubTasksStrategy subTaskStrategy, long parentId) {
		super(id, name, subject, description, status, priority, skipable, actualOwnerId, createdById, createdOn, activationTime, expirationTime, processId, processSessionId, processInstanceId, deploymentId, subTaskStrategy, parentId);
	}

	public ConversationForActionSummaryImpl(long id, String name, String description, Status status, int priority, String actualOwner, String createdBy, Date createdOn, Date activationTime, Date expirationTime, String processId, long processInstanceId, long parentId, String deploymentId, boolean skipable) {
		super(id, name, description, status, priority, actualOwner, createdBy, createdOn, activationTime, expirationTime, processId, processInstanceId, parentId, deploymentId, skipable);
	}

	public ConversationForActionSummaryImpl(long id, String name, String subject, String description, Status status, int priority,
			boolean skipable, User actualOwner, User createdBy, Date createdOn, Date activationTime, Date expirationTime, String processId,
			int processSessionId, long processInstanceId, String deploymentId, SubTasksStrategy subTaskStrategy, long parentId) {
		super(id, name, subject, description, status, priority, skipable, actualOwner, createdBy, createdOn, activationTime, expirationTime,
				processId, processSessionId, processInstanceId, deploymentId, subTaskStrategy, parentId);
	}

}
