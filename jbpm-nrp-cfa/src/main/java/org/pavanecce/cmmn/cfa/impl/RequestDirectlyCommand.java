package org.pavanecce.cmmn.cfa.impl;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForAction;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;
import org.pavanecce.cmmn.cfa.api.NegotiationStep;

public class RequestDirectlyCommand extends AbstractConversationForActionCommand<Void> {

	private static final long serialVersionUID = 11231231414L;

	private NegotiationStep negotiationStep;

	private String ownerId;

	private InternalConversationForAction cfa;

	public RequestDirectlyCommand(String userId, String ownerId, ConversationForAction cfa, NegotiationStep ns) {
		super();
		this.userId = userId;
		this.ownerId = ownerId;
		this.negotiationStep = ns;
		this.cfa = (InternalConversationForAction) cfa;
	}

	@Override
	public Void execute() {
		super.userGroupCallbackHelper.doCallbackUserOperation(userId, taskContext);
		super.userGroupCallbackHelper.doCallbackGroupOperation(ownerId, taskContext);
		super.userGroupCallbackHelper.doCallbackUserOperation(ownerId, taskContext);
		super.userGroupCallbackHelper.doCallbackOperationForPeopleAssignments((InternalPeopleAssignments) cfa.getPeopleAssignments(),
				taskContext);
		InternalTaskData taskData = (InternalTaskData) cfa.getTaskData();
		if (taskData == null) {
			taskData = (InternalTaskData) TaskModelProvider.getFactory().newTaskData();
			cfa.setTaskData(taskData);
		}
		taskData.setDocumentContentId(ensureContentIdPresent(cfa, taskData.getDocumentContentId(), negotiationStep.getInput()));
		taskData.setStatus(Status.Ready);
		InternalPeopleAssignments peopleAssignments = (InternalPeopleAssignments) cfa.getPeopleAssignments();
		if (peopleAssignments == null) {
			peopleAssignments = (InternalPeopleAssignments) TaskModelProvider.getFactory().newPeopleAssignments();
			cfa.setPeopleAssignments(peopleAssignments);
		}
		peopleAssignments.setTaskInitiator(getTaskIdentityService().getUserById(userId));
		OrganizationalEntity owner = super.getTaskIdentityService().getOrganizationalEntityById(ownerId);
		if(!isOwnerPotentialOwner(peopleAssignments)){
			List<OrganizationalEntity> potentialOwners=new ArrayList<OrganizationalEntity>(peopleAssignments.getPotentialOwners());
			potentialOwners.add(owner);
			peopleAssignments.setPotentialOwners(potentialOwners);
		}
		super.userGroupCallbackHelper.doCallbackOperationForTaskData(taskData, taskContext);
		super.userGroupCallbackHelper.doCallbackOperationForTaskDeadlines(((InternalTask) cfa).getDeadlines(), taskContext);
		if (!(isUserInitator(cfa) || isUserBusinessAdministrator(cfa))) {
			throw new IllegalStateException(userId + " is neither the initiator or business administrator");
		}
		persist(cfa);
		ConversationActImpl act = new ConversationActImpl();
		act.setConversationForAction(cfa);
		act.setDateOfCommencement(negotiationStep.getDateOfCommencement());
		act.setDateOfCompletion(negotiationStep.getDateOfCompletion());
		act.setInputContentId(ensureContentIdPresent(cfa, act.getInputContentId(),  negotiationStep.getInput()));
		act.setOutputContentId(ensureContentIdPresent(cfa, act.getOutputContentId(), negotiationStep.getOutput()));
		act.setKind(ConversationActKind.REQUEST);
		act.setOwner(owner);
		act.setResponsePending(true);
		act.setAddressedTo(act.getOwner());
		act.setActor(peopleAssignments.getTaskInitiator());
		act.setResultingConversationState(ConversationForActionState.REQUESTED);
		super.fireBeforeTaskAddedEvent(cfa);
		super.persist(act);
		cfa.setRequest(act);
		cfa.setCurrentAct(act);
		super.taskPersistenceContext.updateTask(cfa);
		super.fireAfterTaskAddedEvent(cfa);
		return null;
	}

	protected boolean isOwnerPotentialOwner(InternalPeopleAssignments peopleAssignments) {
		boolean ownerIsPotentialOwner=false;
		if (peopleAssignments.getPotentialOwners() != null) {
			for (OrganizationalEntity oe: peopleAssignments.getPotentialOwners()) {
				if(oe instanceof User && oe.getId().equals(ownerId)){
					ownerIsPotentialOwner=true;
					break;
				}
			}
		}
		return ownerIsPotentialOwner;
	}

}
