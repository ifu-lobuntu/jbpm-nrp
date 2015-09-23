package org.pavanecce.cmmn.cfa.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ConversationForActionService {
	Collection<ConversationForActionSummary> getPotentialWork(String userId);
	Collection<ConversationForActionSummary> getOwnedCommitments(String userId);
	Collection<ConversationForActionSummary> getInitiatedCommitments(String userId);
	List<ConversationActSummary> getPendingRequests(String userId);
	List<ConversationActSummary> getDisputes(String userId);
	void addConversation(String userId, ConversationForAction c, NegotiationStep request);
	void requestDirectly(String userId,  String ownerId, ConversationForAction c, NegotiationStep request);
	Map<String,Object> getContentMap(long conversationForActionId,long contentId);
	void promise(String userId,  long conversationActId, String comment);
	void counterToInitiator(String userId,  NegotiationStep request);
	void counterToOwner(String userId,  NegotiationStep request);
	void withdraw(String userId,  long conversationActId, Map<String, Object> fault, String comment);
	void reject(String userId,  long conversationActId, Map<String, Object> fault, String comment);
	void accept(String userId,  long conversationActId, String comment);
	void start(String userId,  long conversationActId, String comment);
	void renegotiate(String userId,  NegotiationStep request);
	void acceptNewTerms(String userId,  long conversationActId, String comment);
	void counterToRenegotiator(String userId,  NegotiationStep request);
	void counterFromRenegotiator(String userId,  NegotiationStep request);
	void renege(String userId,  long conversationActId, Map<String, Object> fault, String comment);
	void assertComplete(String userId,  long conversationActId, Map<String, Object> output, String comment);
	void declareInadequate(String userId,  long conversationActId, Map<String, Object> fault, String comment);
	void declareAdequate(String userId,  long conversationActId, String comment);
	
}
