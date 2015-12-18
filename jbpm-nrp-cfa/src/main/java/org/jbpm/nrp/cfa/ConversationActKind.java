package org.jbpm.nrp.cfa;
import static org.jbpm.nrp.cfa.ConversationForActionState.*;
public enum ConversationActKind {
	REQUEST(AllowedConversationRole.Initiator),
	PROMISE(AllowedConversationRole.Owner, REQUESTED),
	COUNTER_TO_INITIATOR(AllowedConversationRole.Owner, REQUESTED),
	COUNTER_TO_OWNER(AllowedConversationRole.Initiator, COUNTERED),
	ACCEPT(AllowedConversationRole.Initiator, PROMISED, COUNTERED),
	START(AllowedConversationRole.Owner,COMMITTED),
	WITHDRAW(AllowedConversationRole.Initiator,REQUESTED,COUNTERED, PROMISED, IN_PROGRESS,RENEGOTIATION_REQUESTED,AWAITING_ACCEPTANCE),
	REJECT(AllowedConversationRole.Owner,REQUESTED),
	ASSERT(AllowedConversationRole.Owner,IN_PROGRESS),
	DECLARE_INADEQUATE(AllowedConversationRole.Initiator,AWAITING_ACCEPTANCE),
	DECLARE_ADEQUATE(AllowedConversationRole.Initiator,AWAITING_ACCEPTANCE),
	RENEGOTIATE(AllowedConversationRole.Any, IN_PROGRESS),
	RENEGE(AllowedConversationRole.Owner,IN_PROGRESS, RENEGOTIATION_REQUESTED),
	COUNTER_TO_RENEGOTIATOR(AllowedConversationRole.CounterNegotiator,RENEGOTIATION_REQUESTED),
	COUNTER_FROM_RENEGOTIATOR(AllowedConversationRole.Renegotiator,COUNTERED),
	ACCEPT_NEW_TERMS(AllowedConversationRole.CounterNegotiator,RENEGOTIATION_REQUESTED,COUNTERED);
	private AllowedConversationRole allowed;

	private ConversationForActionState[] prerequisiteState;

	private ConversationActKind(AllowedConversationRole allowed, ConversationForActionState... prerequisiteState) {
		this.allowed = allowed;
		this.prerequisiteState = prerequisiteState;
	}

	public AllowedConversationRole getAllowedRole() {
		return allowed;
	}

	public ConversationForActionState[] getPrerequisiteState() {
		return prerequisiteState;
	}
}
