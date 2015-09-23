package org.pavanecce.cmmn.cfa.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbpm.cmmn.task.additional.commands.AbstractTaskCommand;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalContent;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.cfa.api.ConversationAct;
import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationForAction;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;
import org.pavanecce.cmmn.cfa.api.InternalConversationAct;
import org.pavanecce.cmmn.cfa.api.InternalConversationForAction;

public abstract class AbstractConversationForActionCommand<T> extends AbstractTaskCommand<T> {

	private static final long serialVersionUID = -4217142163951701835L;

	protected UserGroupCallbackHelper userGroupCallbackHelper = new UserGroupCallbackHelper();

	private List<String> groupsForUser;

	public AbstractConversationForActionCommand() {
		super();
	}

	protected ConversationActImpl createResponseCopy(long previousId, ConversationActKind kind) {
		ConversationActImpl previous = find(ConversationActImpl.class, previousId);
		return createResponseCopy(previous, kind);
	}

	protected ConversationActImpl createResponseCopy(InternalConversationAct previous, ConversationActKind kind) {
		validatePreviousState(previous, kind);
		validateRole(previous, kind);
		ConversationActImpl result = new ConversationActImpl();
		result.setKind(kind);
		InternalConversationForAction icfa = (InternalConversationForAction) previous.getConversationForAction();
		if(updatesCurrentAct(kind, icfa)){
			icfa.setCurrentAct(result);
			previous.setResponsePending(false);
		}
		result.setConversationForAction(icfa);
		result.setFaultContentId(maybeCloneContent(previous.getFaultContentId()));
		result.setFaultName(previous.getFaultName());
		result.setFaultType(previous.getFaultType());
		result.setInputContentId(maybeCloneContent(previous.getInputContentId()));
		result.setOutputContentId(maybeCloneContent(previous.getOutputContentId()));
		result.setCommitted(previous.isCommitted());
		result.setOwner(previous.getOwner());
		result.setRenegotiator(previous.getRenegotiator());
		result.setResultingConversationState(previous.getResultingConversationState());
		result.setPreviousStep(previous);
		result.setActor(new UserImpl(userId));
		result.setAddressedTo(previous.getActor());
		if (previous.getDateOfCommencement() != null) {
			result.setDateOfCommencement(new Date(previous.getDateOfCommencement().getTime()));
		}
		if (previous.getDateOfCompletion() != null) {
			result.setDateOfCompletion(new Date(previous.getDateOfCompletion().getTime()));
		}
		result.setResponsePending(true);
		return result;
	}

	protected void validateRole(InternalConversationAct previous, ConversationActKind kind) {
		switch (kind.getAllowedRole()) {
		case Any:
			//fine
			break;
		case CounterNegotiator:
			//the participant that is NOT the negotiator
			break;
		case Initiator:
			String initatiorId = previous.getConversationForAction().getPeopleAssignments().getTaskInitiator().getId();
			if(!userId.equals(initatiorId)){
				throw new IllegalStateException("Only the initiator, '" + initatiorId +"' is allowed to " +kind.name());
			}
			break;
		case Owner:
			String ownerId = previous.getOwner().getId();
			if(!userId.equals(ownerId)){
				throw new IllegalStateException("Only the owner, '" + ownerId +"' is allowed to " +kind.name());
			}
			break;
		case Renegotiator:
			//negotiaor
			break;
		}
	}

	protected boolean updatesCurrentAct(ConversationActKind kind, InternalConversationForAction icfa) {
		boolean updateCurrentAct=false;
		if (kind == ConversationActKind.COUNTER_TO_INITIATOR || kind == ConversationActKind.COUNTER_TO_OWNER
				|| kind == ConversationActKind.PROMISE) {
			if (icfa.wasRequestedDirectly()) {
				updateCurrentAct=true;
			}
		} else {
			updateCurrentAct=true;
		}
		return updateCurrentAct;
	}

	protected void validatePreviousState(InternalConversationAct previous, ConversationActKind kind) {
		boolean valid = false;
		for (ConversationForActionState s : kind.getPrerequisiteState()) {
			if (s == previous.getResultingConversationState()) {
				valid = true;
			}
		}
		if (!valid) {
			throw new IllegalStateException("Previously the conversation was in the '" + previous.getResultingConversationState().name()
					+ "' state, therefore the act '" + kind.name() + "' cannot be executed against it");
		}
	}

	protected void acceptCommitment(ConversationAct accept) {
		InternalConversationForAction cfa = (InternalConversationForAction) accept.getConversationForAction();
		cfa.setCommitment(accept);
		InternalTaskData itd = (InternalTaskData) cfa.getTaskData();
		super.taskId = cfa.getId();
		Map<String, Object> inputMap = Collections.<String, Object> singletonMap("key", getContentAsMap(accept.getInputContentId()));
		itd.setDocumentContentId(ensureContentIdPresent(cfa, itd.getDocumentContentId(), inputMap, "key"));
		Map<String, Object> outputMap = Collections.<String, Object> singletonMap("key", getContentAsMap(accept.getOutputContentId()));
		itd.setOutputContentId(ensureContentIdPresent(cfa, itd.getOutputContentId(), outputMap, "key"));
		// TODO implement deadlines implied by dateOfCommencement and
		// dateOfCompletion
	}

	private long maybeCloneContent(long contentId) {
		if (contentId >= 0) {
			Content contentById = super.taskContext.getTaskContentService().getContentById(contentId);
			if (contentById != null) {
				InternalContent newContent = (InternalContent) TaskModelProvider.getFactory().newContent();
				newContent.setContent(contentById.getContent());
				super.persist(newContent);
				return newContent.getId();
			}
		}
		return -1;
	}

	protected long ensureContentIdPresent(ConversationForAction cfa, long contentId, Map<String, Object> input) {
		Map<String, Object> map = Collections.<String, Object> singletonMap("key", input);
		long ensureContentIdPresent = ensureContentIdPresent(cfa, contentId, map, "key");
		return ensureContentIdPresent;
	}

	private List<String> getUsersGroups() {
		if (this.groupsForUser == null) {
			this.groupsForUser = this.taskContext.getUserGroupCallback().getGroupsForUser(userId, null, null);
		}
		return groupsForUser;
	}

	protected boolean isUserBusinessAdministrator(ConversationForAction cfa) {

		List<OrganizationalEntity> orgEntities = cfa.getPeopleAssignments().getBusinessAdministrators();
		boolean isUserInOrgEntities = false;
		if (orgEntities != null) {
			for (OrganizationalEntity oe : orgEntities) {
				if (oe instanceof User) {
					if (oe.getId().equals(userId)) {
						isUserInOrgEntities = true;
						break;
					}
				} else {
					if (getUsersGroups().contains(oe.getId())) {
						isUserInOrgEntities = true;
						break;
					}
				}
			}
		}
		return isUserInOrgEntities;
	}

	protected boolean isUserInitator(ConversationForAction cfa) {
		boolean isUserInitiator = false;
		if (cfa.getPeopleAssignments().getTaskInitiator() != null && cfa.getPeopleAssignments().getTaskInitiator().getId().equals(userId)) {
			isUserInitiator = true;
		}
		return isUserInitiator;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getContentAsMap(long contentId2) {
		Content c = taskContext.getTaskContentService().getContentById(contentId2);
		if (c == null) {
			return Collections.emptyMap();
		} else {
			ContentMarshallerContext mc = taskContext.getTaskContentService().getMarshallerContext(getTaskById(taskId));
			return (Map<String, Object>) ContentMarshallerHelper.unmarshall(c.getContent(), mc.getEnvironment());
		}
	}

}