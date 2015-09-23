package org.pavanecce.cmmn.cfa.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kie.internal.task.api.InternalTaskService;
import org.pavanecce.cmmn.cfa.api.ConversationActSummary;
import org.pavanecce.cmmn.cfa.api.ConversationForAction;
import org.pavanecce.cmmn.cfa.api.ConversationForActionService;
import org.pavanecce.cmmn.cfa.api.ConversationForActionSummary;
import org.pavanecce.cmmn.cfa.api.NegotiationStep;

public class ConversationForActionServiceImpl implements ConversationForActionService {
	private InternalTaskService taskService;

	public void setTaskService(InternalTaskService taskService) {
		this.taskService = taskService;
	}

	@Override
	public void requestDirectly(String userId, String ownerId, ConversationForAction cfa, NegotiationStep step) {
		taskService.execute(new RequestDirectlyCommand(userId, ownerId, cfa, step));
	}

	@Override
	public List<ConversationActSummary> getPendingRequests(String userId) {
		return taskService.execute(new GetPendingRequestsCommand(userId));
	}

	@Override
	public void addConversation(String userId, ConversationForAction c, NegotiationStep request) {

	}

	@Override
	public Collection<ConversationForActionSummary> getPotentialWork(String userId) {
		return taskService.execute(new GetPotentialWorkCommand(userId));
	}


	@Override
	public void promise(String userId, long conversationActId, String comment) {
		taskService.execute(new PromiseCommand(userId,conversationActId,comment));
	}

	@Override
	public void counterToInitiator(String userId, NegotiationStep request) {
		taskService.execute(new CounterToInitiatorCommand(userId,request));
	}

	@Override
	public void counterToOwner(String userId, NegotiationStep request) {
		taskService.execute(new CounterToOwnerCommand(userId,request));
	}

	@Override
	public void withdraw(String userId, long conversationActId, Map<String, Object> fault, String comment) {
		taskService.execute(new WithdrawCommand(userId,conversationActId, fault, comment));
	}

	@Override
	public void reject(String userId, long conversationActId, Map<String, Object> fault, String comment) {
		taskService.execute(new RejectCommand(userId,conversationActId, fault, comment));
	}

	@Override
	public void accept(String userId, long conversationActId, String comment) {
		taskService.execute(new AcceptCommand(userId, conversationActId, comment));
	}

	@Override
	public void start(String userId, long conversationActId, String comment) {
		taskService.execute(new StartCommand(userId, conversationActId, comment));
	}

	@Override
	public void renegotiate(String userId, NegotiationStep request) {
		taskService.execute(new RenegotiateCommand(userId,request));
	}

	@Override
	public void acceptNewTerms(String userId, long conversationActId, String comment) {
		taskService.execute(new AcceptNewTermsCommand(userId, conversationActId, comment));
	}

	@Override
	public void counterToRenegotiator(String userId, NegotiationStep request) {
	}

	@Override
	public void counterFromRenegotiator(String userId, NegotiationStep request) {
	}

	@Override
	public void renege(String userId, long conversationActId, Map<String, Object> fault, String comment) {
		taskService.execute(new RenegeCommand(userId, conversationActId, fault, comment));
	}

	@Override
	public void assertComplete(String userId, long conversationActId, Map<String, Object> output, String comment) {
		taskService.execute(new AssertCompleteCommand(userId, conversationActId, output, comment));
	}

	@Override
	public void declareInadequate(String userId, long conversationActId, Map<String, Object> fault, String comment) {
		taskService.execute(new DeclareInadequateCommand(userId, conversationActId, fault, comment));
	}

	@Override
	public void declareAdequate(String userId, long conversationActId, String comment) {
		taskService.execute(new DeclareAdequateCommand(userId, conversationActId, comment));
	}

	@Override
	public Map<String, Object> getContentMap(long conversationForActionId, long contentId) {
		return taskService.execute(new GetContentMapCommand(conversationForActionId, contentId));
	}

	@Override
	public List<ConversationForActionSummary> getOwnedCommitments(String userId) {
		return taskService.execute(new GetOwnedCommitmentsCommand(userId));
	}

	@Override
	public List<ConversationForActionSummary> getInitiatedCommitments(String userId) {
		return taskService.execute(new GetInitiatedCommitmentsCommand(userId));
	}

	@Override
	public List<ConversationActSummary> getDisputes(String userId) {
		return taskService.execute(new GetDisputesCommand(userId));
	}
}
