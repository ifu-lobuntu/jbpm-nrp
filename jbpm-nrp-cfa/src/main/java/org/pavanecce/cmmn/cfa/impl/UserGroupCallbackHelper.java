package org.pavanecce.cmmn.cfa.impl;

import java.util.List;

import org.jbpm.services.task.commands.UserGroupCallbackTaskCommand;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTaskData;
/**
 * This class exposes the UserGroupCallbackTaskCommand methods publicly. This allows it to be used via containment-delegation to circumvent the need for dual inheritance 
 *
 */
public class UserGroupCallbackHelper extends UserGroupCallbackTaskCommand<Void> {

	private static final long serialVersionUID = 2573254432505147078L;

	@Override
	public  List<String> doUserGroupCallbackOperation(String userId, List<String> groupIds, TaskContext context) {
		return super.doUserGroupCallbackOperation(userId, groupIds, context);
	}

	@Override
	public  boolean doCallbackUserOperation(String userId, TaskContext context) {
		return super.doCallbackUserOperation(userId, context);
	}

	@Override
	public  boolean doCallbackGroupOperation(String groupId, TaskContext context) {
		return super.doCallbackGroupOperation(groupId, context);
	}

	@Override
	public  void addUserFromCallbackOperation(String userId, TaskContext context) {
		super.addUserFromCallbackOperation(userId, context);
	}

	@Override
	public  void persistIfNotExists(OrganizationalEntity entity, TaskContext context) {
		super.persistIfNotExists(entity, context);
	}

	@Override
	public  List<String> doCallbackGroupsOperation(String userId, List<String> groupIds, TaskContext context) {
		return super.doCallbackGroupsOperation(userId, groupIds, context);
	}

	@Override
	public  void addGroupFromCallbackOperation(String groupId, TaskContext context) {
		super.addGroupFromCallbackOperation(groupId, context);
	}

	@Override
	public  void doCallbackOperationForTaskData(InternalTaskData data, TaskContext context) {
		super.doCallbackOperationForTaskData(data, context);
	}

	@Override
	public  void doCallbackOperationForPotentialOwners(List<OrganizationalEntity> potentialOwners, TaskContext context) {
		super.doCallbackOperationForPotentialOwners(potentialOwners, context);
	}

	@Override
	public  void doCallbackOperationForPeopleAssignments(InternalPeopleAssignments assignments, TaskContext context) {
		super.doCallbackOperationForPeopleAssignments(assignments, context);
	}

	@Override
	public  void doCallbackOperationForTaskDeadlines(Deadlines deadlines, TaskContext context) {
		super.doCallbackOperationForTaskDeadlines(deadlines, context);
	}

	@Override
	public  void doCallbackOperationForComment(Comment comment, TaskContext context) {
		super.doCallbackOperationForComment(comment, context);
	}

	@Override
	public  void doCallbackOperationForAttachment(Attachment attachment, TaskContext context) {
		super.doCallbackOperationForAttachment(attachment, context);
	}

	@Override
	public  List<String> filterGroups(List<String> groups) {
		return super.filterGroups(groups);
	}
	

}
