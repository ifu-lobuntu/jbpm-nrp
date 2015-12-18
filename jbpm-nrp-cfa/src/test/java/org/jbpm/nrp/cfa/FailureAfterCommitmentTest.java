package org.jbpm.nrp.cfa;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbpm.nrp.cfa.impl.ConversationForActionServiceImpl;
import org.junit.Test;

public class FailureAfterCommitmentTest extends AbstractConversationForActionTest {
	{
		super.isJpa = true;
	}

	public FailureAfterCommitmentTest() {
		super("org.jbpm.persistence.jpa");
	}

	@Test
	public void testInProgressWithdrawal() {
	}

	@Test
	public void testRenegotiationWithdrawal() {
	}

	@Test
	public void testFailedAcceptance() {
	}

	@Test
	public void testRenegotiationRenege() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		super.requestToRenegotiate(service, "Ampie",2000, 7);
		ConversationActSummary renegotiation= service.getPendingRequests("Spielman").get(0);
		//When
		service.renege("Spielman", renegotiation.getActId(), Collections.singletonMap("numberOfWalls", (Object) 8), "I've already built 8. If you don't pay, I don't play anymore.");
		// Then
		ConversationForAction conversation = (ConversationForAction) getTaskService().getTaskById(renegotiation.getConversationId());
		assertEquals(0, service.getPendingRequests("Ampie").size());
		assertEquals(0,service.getPendingRequests("Spieman").size());
		Map<String, Object> faultMap = service.getContentMap(conversation.getId(), conversation.getTaskData().getFaultContentId());
		assertEquals(8, faultMap.get("numberOfWalls"));
		assertEquals(conversation.getCurrentAct().getId(), conversation.getOutcome().getId());
		assertEquals(ConversationForActionState.RENEGUED, conversation.getOutcome().getResultingConversationState());
		assertEquals(ConversationActKind.RENEGE, conversation.getCurrentAct().getKind());
		assertTrue(conversation.getOutcome().isDispute());
	}

	@Test
	public void testInProgressRenege() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		super.requestToAssertComplete(service);
		ConversationActSummary assertion=  service.getPendingRequests("Ampie").get(0);
		service.declareInadequate("Ampie", assertion.getActId(), Collections.singletonMap("numberOfWalls", (Object) 7), "I only saw 7 walls, dude.");
		ConversationActSummary declaration= service.getPendingRequests("Spielman").get(0);
		//When
		service.renege("Spielman", declaration.getActId(), Collections.singletonMap("numberOfWalls", (Object) 9), "The other two walls are there, you can't count.");
		// Then
		ConversationForAction conversation = (ConversationForAction) getTaskService().getTaskById(assertion.getConversationId());
		List<ConversationActSummary> pendingRequests = service.getPendingRequests("Ampie");
		assertEquals(0,pendingRequests.size());
		assertEquals(0,service.getPendingRequests("Spieman").size());
		assertEquals(1,service.getDisputes("Ampie").size());
		assertEquals(1,service.getDisputes("Spielman").size());
		ConversationActSummary dispute1= service.getDisputes("Ampie").get(0);
		ConversationActSummary dispute2= service.getDisputes("Spielman").get(0);
		assertEquals(dispute1.getActId(), dispute2.getActId());
		Map<String, Object> faultMap = service.getContentMap(conversation.getId(), conversation.getTaskData().getFaultContentId());
		assertEquals(9, faultMap.get("numberOfWalls"));
		assertEquals(conversation.getCurrentAct().getId(), dispute1.getActId());
		assertEquals(conversation.getOutcome().getId(), dispute1.getActId());
		assertEquals(ConversationForActionState.RENEGUED, conversation.getCurrentAct().getResultingConversationState());
		assertEquals(ConversationActKind.RENEGE, conversation.getCurrentAct().getKind());
	}

	@Test
	public void testDeclareInadequate() throws Exception{
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		super.requestToAssertComplete(service);
		ConversationActSummary assertion= service.getPendingRequests("Ampie").get(0);
		//When
		service.declareInadequate("Ampie", assertion.getActId(), Collections.singletonMap("numberOfWalls", (Object) 7), "I only saw 7 walls, dude.");
		// Then
		ConversationForAction conversation = (ConversationForAction) getTaskService().getTaskById(assertion.getConversationId());
		ConversationActSummary declaration= service.getPendingRequests("Spielman").get(0);
		Map<String, Object> faultMap = service.getContentMap(conversation.getId(), conversation.getTaskData().getFaultContentId());
		assertEquals(7, faultMap.get("numberOfWalls"));
		assertEquals(conversation.getCurrentAct().getId(), declaration.getActId());
		assertEquals(ConversationForActionState.IN_PROGRESS, conversation.getCurrentAct().getResultingConversationState());
		assertEquals(ConversationActKind.DECLARE_INADEQUATE, conversation.getCurrentAct().getKind());
	}
}
