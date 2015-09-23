package org.pavanecce.cmmn.cfa.api;

import static org.pavanecce.cmmn.cfa.api.AllowedConversationRole.Any;
import static org.pavanecce.cmmn.cfa.api.AllowedConversationRole.CounterNegotiator;
import static org.pavanecce.cmmn.cfa.api.AllowedConversationRole.Initiator;
import static org.pavanecce.cmmn.cfa.api.AllowedConversationRole.Owner;
import static org.pavanecce.cmmn.cfa.api.AllowedConversationRole.Renegotiator;
import static org.pavanecce.cmmn.cfa.api.ConversationForActionState.*;

public enum ConversationActKind {
	REQUEST(Initiator),
	PROMISE(Owner, REQUESTED),
	COUNTER_TO_INITIATOR(Owner, REQUESTED),
	COUNTER_TO_OWNER(Initiator, COUNTERED),
	ACCEPT(Initiator, PROMISED, COUNTERED),
	START(Owner,COMMITTED),
	WITHDRAW(Initiator,REQUESTED,COUNTERED, PROMISED, IN_PROGRESS,RENEGOTIATION_REQUESTED,AWAITING_ACCEPTANCE),
	REJECT(Owner,REQUESTED),
	ASSERT(Owner,IN_PROGRESS),
	DECLARE_INADEQUATE(Initiator,AWAITING_ACCEPTANCE),
	DECLARE_ADEQUATE(Initiator,AWAITING_ACCEPTANCE),
	RENEGOTIATE(Any, IN_PROGRESS),
	RENEGE(Owner,IN_PROGRESS, RENEGOTIATION_REQUESTED),
	COUNTER_TO_RENEGOTIATOR(CounterNegotiator,RENEGOTIATION_REQUESTED),
	COUNTER_FROM_RENEGOTIATOR(Renegotiator,COUNTERED),
	ACCEPT_NEW_TERMS(CounterNegotiator,RENEGOTIATION_REQUESTED,COUNTERED);
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
