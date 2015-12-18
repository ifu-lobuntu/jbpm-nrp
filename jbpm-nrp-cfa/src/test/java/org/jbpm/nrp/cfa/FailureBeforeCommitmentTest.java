package org.jbpm.nrp.cfa;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbpm.nrp.cfa.impl.ConversationForActionServiceImpl;
import org.junit.Test;

public class FailureBeforeCommitmentTest extends AbstractConversationForActionTest {
	{
		super.isJpa = true;
	}

	public FailureBeforeCommitmentTest() {
		super();
	}

	@Test
	public void testCounter() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		requestAndCounter(service);
		// Then
		ConversationActSummary counter = service.getPendingRequests("Ampie").get(0);
		assertEquals(Timestamp.valueOf("2014-09-15 14:22:00.0"), counter.getDateOfCommencement());
		assertEquals(Timestamp.valueOf("2015-09-15 14:22:00.0"), counter.getDateOfCompletion());
		Map<String, Object> inputMap = service.getContentMap(counter.getConversationId(), counter.getInputContentId());
		assertEquals(5000, inputMap.get("numberOfBricks"));
		Map<String, Object> outputMap = service.getContentMap(counter.getConversationId(), counter.getOutputContentId());
		assertEquals(9, outputMap.get("numberOfWalls"));
		ConversationForAction conversation=(ConversationForAction) getTaskService().getTaskById(counter.getConversationId());
		assertEquals(conversation.getCurrentAct().getId(), counter.getActId());
		assertEquals(ConversationForActionState.COUNTERED, conversation.getCurrentAct().getResultingConversationState());
		assertEquals(ConversationActKind.COUNTER_TO_INITIATOR, conversation.getCurrentAct().getKind());
	}
	@Test
	public void testCounterCounter() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		requestCounterAndCounter(service);
		// Then
		ConversationActSummary counterCounter = service.getPendingRequests("Spielman").get(0);
		assertEquals(Timestamp.valueOf("2014-09-15 10:22:00.0"), counterCounter.getDateOfCommencement());
		assertEquals(Timestamp.valueOf("2015-09-15 10:22:00.0"), counterCounter.getDateOfCompletion());
		Map<String, Object> inputMap = service.getContentMap(counterCounter.getConversationId(), counterCounter.getInputContentId());
		assertEquals(4500, inputMap.get("numberOfBricks"));
		Map<String, Object> outputMap = service.getContentMap(counterCounter.getConversationId(), counterCounter.getOutputContentId());
		assertEquals(9, outputMap.get("numberOfWalls"));
		ConversationForAction conversation=(ConversationForAction) getTaskService().getTaskById(counterCounter.getConversationId());
		assertEquals(conversation.getCurrentAct().getId(), counterCounter.getActId());
		assertEquals(ConversationForActionState.REQUESTED, conversation.getCurrentAct().getResultingConversationState());
		assertEquals(ConversationActKind.COUNTER_TO_OWNER, conversation.getCurrentAct().getKind());
	}
	@Test
	public void testCounterWithdraw() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		requestAndCounter(service);
		ConversationActSummary counter = service.getPendingRequests("Ampie").get(0);
		//TODO think a bit more about what a Fault would mean here: maybe the minimum requirements from the initiator's perspective
		//When
		service.withdraw("Ampie", counter.getActId(), Collections.singletonMap("numberOfBricks", (Object) 4500), "I can only afford 4500 bricks");
		// Then
		ConversationForAction conversation = (ConversationForAction) getTaskService().getTaskById(counter.getConversationId());
		Map<String, Object> faultMap = service.getContentMap(conversation.getId(), conversation.getTaskData().getFaultContentId());
		assertEquals(4500, faultMap.get("numberOfBricks"));
		assertEquals(conversation.getTaskData().getFaultContentId(), conversation.getOutcome().getFaultContentId());
		assertEquals(conversation.getCurrentAct().getId(), conversation.getOutcome().getId());
		assertEquals(ConversationForActionState.NEGOTIATION_FAILED, conversation.getOutcome().getResultingConversationState());
		assertEquals(ConversationActKind.WITHDRAW, conversation.getOutcome().getKind());
	}
	@Test
	public void testRequestReject() {
		// Given
		ConversationForActionServiceImpl service = createConversationService();
		requestDirectly(service);
		ConversationActSummary counter = service.getPendingRequests("Spielman").get(0);
		//TODO think a bit more about what a Fault would mean here: maybe the minimum requirements from the owner's perspective
		//When
		service.reject("Spielman", counter.getActId(), Collections.singletonMap("numberOfBricks", (Object) 5000), "I can only do it with 5000 bricks");
		// Then
		ConversationForAction conversation = (ConversationForAction) getTaskService().getTaskById(counter.getConversationId());
		Map<String, Object> faultMap = service.getContentMap(conversation.getId(), conversation.getTaskData().getFaultContentId());
		assertEquals(5000, faultMap.get("numberOfBricks"));
		assertEquals(conversation.getTaskData().getFaultContentId(), conversation.getOutcome().getFaultContentId());
		assertEquals(conversation.getCurrentAct().getId(), conversation.getOutcome().getId());
		assertEquals(ConversationForActionState.NEGOTIATION_FAILED, conversation.getOutcome().getResultingConversationState());
		assertEquals(ConversationActKind.REJECT, conversation.getOutcome().getKind());
	}
	protected void requestCounterAndCounter(ConversationForActionServiceImpl service) {
		requestAndCounter(service);
		ConversationActSummary counter = service.getPendingRequests("Ampie").get(0);
		NegotiationStep step = new NegotiationStep();
		step.setPreviousActId(counter.getActId());
		step.setComment("What about 10 o' clock and 500 fewer bricks?");
		step.setDateOfCommencement(Timestamp.valueOf("2014-09-15 10:22:00.0"));
		step.setDateOfCompletion(Timestamp.valueOf("2015-09-15 10:22:00.0"));
		step.setInput(Collections.singletonMap("numberOfBricks", (Object) 4500));
		//When
		service.counterToOwner("Ampie", step);
	}

	protected void requestAndCounter(ConversationForActionServiceImpl service) {
		requestDirectly(service);
		List<ConversationActSummary> directRequests = service.getPendingRequests("Spielman");
		ConversationActSummary request = directRequests.get(0);
		NegotiationStep step = new NegotiationStep();
		step.setPreviousActId(request.getActId());
		step.setComment("What about the 15th with more bricks and fewer walls.");
		step.setDateOfCommencement(Timestamp.valueOf("2014-09-15 14:22:00.0"));
		step.setDateOfCompletion(Timestamp.valueOf("2015-09-15 14:22:00.0"));
		step.setInput(Collections.singletonMap("numberOfBricks", (Object) 5000));
		step.setOutput(Collections.singletonMap("numberOfWalls", (Object) 9));
		//When
		service.counterToInitiator("Spielman", step);
	}
}
