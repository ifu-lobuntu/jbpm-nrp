package org.pavanecce.cmmn.cfa.api;

import org.pavanecce.cmmn.cfa.impl.ConversationActImpl;
import static org.pavanecce.cmmn.cfa.api.ConversationActKind.*;

public enum ConversationForActionState {
	REQUESTED(PROMISE, COUNTER_TO_INITIATOR) {
		public void promise(ConversationActImpl n) {
			n.setResultingConversationState(PROMISED);
		}

		public void withdraw(ConversationActImpl n) {
			n.setResultingConversationState(NEGOTIATION_FAILED);
		}

		public void reject(ConversationActImpl n) {
			n.setResultingConversationState(NEGOTIATION_FAILED);
		}

		public void counterToInitiator(ConversationActImpl n) {
			n.setResultingConversationState(COUNTERED);
		}
	},
	PROMISED {
		public void accept(ConversationActImpl n) {
			n.setResultingConversationState(COMMITTED);
		}
	},
	COUNTERED {
		public void accept(ConversationActImpl n) {
			n.setResultingConversationState(COMMITTED);
		}

		public void withdraw(ConversationActImpl n) {
			if (n.isCommitted()) {
				n.setResultingConversationState(WITHDRAWN);
			} else {
				n.setResultingConversationState(NEGOTIATION_FAILED);
			}
		}

		public void counterToOwner(ConversationActImpl n) {
			if (n.isCommitted()) {
				throw new UnsupportedOperationException("counterToB can only be called if the negotation is not yet committed to");
			} else {
				n.setResultingConversationState(REQUESTED);
			}
		}

		@Override
		public void counterToRenegotiator(ConversationActImpl n) {
			if (n.isCommitted()) {
				n.setResultingConversationState(RENEGOTIATION_REQUESTED);
			} else {
				throw new UnsupportedOperationException(
						"counterToRenegotiator can only be called if the negotation was previously committed to");
			}
		}
	},
	NEGOTIATION_FAILED {
	},
	COMMITTED {
		@Override
		public void start(ConversationActImpl n) {
			n.setResultingConversationState(IN_PROGRESS);
		}
	},
	RENEGOTIATION_REQUESTED {
		public void acceptNewTerms(ConversationActImpl n) {
			n.setResultingConversationState(COMMITTED);
		}

		public void withdraw(ConversationActImpl n) {
			n.setResultingConversationState(NEGOTIATION_FAILED);
		}

		public void reject(ConversationActImpl n) {
			n.setResultingConversationState(NEGOTIATION_FAILED);
		}

		public void counterToInitiator(ConversationActImpl n) {
			n.setResultingConversationState(COUNTERED);
		}
	},
	IN_PROGRESS {
		@Override
		public void assertCompletion(ConversationActImpl n) {
			n.setResultingConversationState(AWAITING_ACCEPTANCE);
		}
	},
	AWAITING_ACCEPTANCE {
		@Override
		public void declareAdequate(ConversationActImpl n) {
			n.setResultingConversationState(CONSUMMATED);
		}
	},
	CONSUMMATED {
	},
	WITHDRAWN {
	},
	RENEGUED {
	};

	private ConversationActKind[] allowedActs;

	private ConversationForActionState(ConversationActKind... allowedActs) {
		this.allowedActs = allowedActs;
	}

	public boolean isAllowed(ConversationActKind act) {
		for (ConversationActKind conversationActKind : allowedActs) {
			if (conversationActKind == act) {
				return true;
			}
		}
		return false;
	}

	public void promise(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'promise'");
	}

	public void accept(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'accept'");
	}

	public void start(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'start'");
	}

	public void withdraw(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'withdraw'");
	}

	public void reject(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'reject'");

	}

	public void counterToInitiator(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'counterToA'");
	}

	public void counterToOwner(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'counterToB'");
	}

	public void declareAdequate(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'declareAdequate'");
	}

	public void declareInadequate(ConversationAct n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'counterFromRenegotiator'");
	}

	public void assertCompletion(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'assertCompletion'");
	}

	public void renege(ConversationAct n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'renegue'");
	}

	public void acceptNewTerms(ConversationAct n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'renegue'");
	}

	public void counterToRenegotiator(ConversationActImpl n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'counterToRenegotiator'");

	}

	public void counterFromRenegotiator(ConversationAct n) {
		throw new UnsupportedOperationException(this.name() + " does not support the operation 'counterFromRenegotiator'");
	}

}
