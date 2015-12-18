package org.jbpm.nrp.cfa;

import java.sql.Timestamp;
import java.util.Map;

import org.jbpm.nrp.cfa.impl.ConversationForActionServiceImpl;
import org.junit.Test;

public class SuccessfulRenegotiationTest extends AbstractConversationForActionTest {
	{
		super.isJpa = true;
	}

	public SuccessfulRenegotiationTest() {
		super("org.jbpm.persistence.jpa");
	}

	@Test
	public void testRenegotiatedByInitiator() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		requestToRenegotiate(service, "Ampie", 4000,6);
		// Then
		ConversationActSummary renegotiation = service.getPendingRequests("Spielman").get(0);
		assertEquals(Timestamp.valueOf("2014-09-15 14:22:00.0"), renegotiation.getDateOfCommencement());
		assertEquals(Timestamp.valueOf("2015-09-15 14:22:00.0"), renegotiation.getDateOfCompletion());
		Map<String, Object> inputMap = service.getContentMap(renegotiation.getConversationId(), renegotiation.getInputContentId());
		assertEquals(4000, inputMap.get("numberOfBricks"));
		Map<String, Object> outputMap = service.getContentMap(renegotiation.getConversationId(), renegotiation.getOutputContentId());
		assertEquals(6, outputMap.get("numberOfWalls"));
		ConversationForAction conversation = (ConversationForAction) getTaskService().getTaskById(renegotiation.getConversationId());
		assertEquals(conversation.getCurrentAct().getId(), renegotiation.getActId());
		assertEquals(ConversationForActionState.RENEGOTIATION_REQUESTED, conversation.getCurrentAct().getResultingConversationState());
		assertEquals(ConversationActKind.RENEGOTIATE, conversation.getCurrentAct().getKind());
	}

	@Test
	public void testRenegotiatedByOwner() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		requestToRenegotiate(service, "Spielman", 4500,7);
		// Then
		assertEquals(0,service.getPendingRequests("Spielman").size());
		assertEquals(1,service.getPendingRequests("Ampie").size());
		ConversationActSummary renegotiation = service.getPendingRequests("Ampie").get(0);
		assertEquals(Timestamp.valueOf("2014-09-15 14:22:00.0"), renegotiation.getDateOfCommencement());
		assertEquals(Timestamp.valueOf("2015-09-15 14:22:00.0"), renegotiation.getDateOfCompletion());
		Map<String, Object> inputMap = service.getContentMap(renegotiation.getConversationId(), renegotiation.getInputContentId());
		assertEquals(4500, inputMap.get("numberOfBricks"));
		Map<String, Object> outputMap = service.getContentMap(renegotiation.getConversationId(), renegotiation.getOutputContentId());
		assertEquals(7, outputMap.get("numberOfWalls"));
		ConversationForAction conversation = (ConversationForAction) getTaskService().getTaskById(renegotiation.getConversationId());
		assertEquals(conversation.getCurrentAct().getId(), renegotiation.getActId());
		assertEquals(ConversationForActionState.RENEGOTIATION_REQUESTED, conversation.getCurrentAct().getResultingConversationState());
		assertEquals(ConversationActKind.RENEGOTIATE, conversation.getCurrentAct().getKind());
	}
	@Test
	public void testAcceptNewTerms() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		requestToRenegotiate(service, "Spielman", 4100,6);
		ConversationActSummary renegotiation = service.getPendingRequests("Ampie").get(0);
		//When
		service.acceptNewTerms("Ampie", renegotiation.getActId(), "OK Sure");
		// Then
		assertEquals(0,service.getPendingRequests("Spielman").size());
		assertEquals(0,service.getPendingRequests("Ampie").size());
		assertEquals(1,service.getOwnedCommitments("Spielman").size());
		assertEquals(1,service.getInitiatedCommitments("Ampie").size());
		ConversationAct acceptanceOfNewTerms =  ((ConversationForAction) getTaskService().getTaskById(renegotiation.getConversationId())).getCommitment();
		assertEquals(Timestamp.valueOf("2014-09-15 14:22:00.0"), acceptanceOfNewTerms.getDateOfCommencement());
		assertEquals(Timestamp.valueOf("2015-09-15 14:22:00.0"), acceptanceOfNewTerms.getDateOfCompletion());
		Map<String, Object> inputMap = service.getContentMap(renegotiation.getConversationId(), acceptanceOfNewTerms.getInputContentId());
		assertEquals(4100, inputMap.get("numberOfBricks"));
		Map<String, Object> outputMap = service.getContentMap(renegotiation.getConversationId(), acceptanceOfNewTerms.getOutputContentId());
		assertEquals(6, outputMap.get("numberOfWalls"));
		ConversationForAction conversation = (ConversationForAction) getTaskService().getTaskById(renegotiation.getConversationId());
		assertEquals(conversation.getCurrentAct().getId(), acceptanceOfNewTerms.getId());
		assertEquals(conversation.getCurrentAct().getId(), conversation.getCommitment().getId());
		assertEquals(ConversationForActionState.IN_PROGRESS, conversation.getCurrentAct().getResultingConversationState());
		assertEquals(ConversationActKind.ACCEPT_NEW_TERMS, conversation.getCurrentAct().getKind());
	}

	@Test
	public void testRenegotiationCounteredByInitiator() {
	}

	@Test
	public void testRenegotiationCounteredByOwner() {
	}


}
