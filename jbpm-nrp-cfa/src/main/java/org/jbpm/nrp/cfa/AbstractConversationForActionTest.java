package org.jbpm.nrp.cfa;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import org.jbpm.cmmn.test.AbstractCmmnCaseTestCase;
import org.jbpm.nrp.cfa.impl.ConversationForActionImpl;
import org.jbpm.nrp.cfa.impl.ConversationForActionServiceImpl;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.kie.api.task.model.TaskSummary;

public abstract class AbstractConversationForActionTest extends AbstractCmmnCaseTestCase {

	public AbstractConversationForActionTest(String persistenceUnitName) {
		super(persistenceUnitName);
	}
	public AbstractConversationForActionTest(){
		super("org.jbpm.persistence.jpa");
	}

	protected ConversationActSummary requestAndPromise(ConversationForActionServiceImpl service) {
		requestDirectly(service);
		List<ConversationActSummary> directRequests = service.getPendingRequests("Spielman");
		ConversationActSummary request = directRequests.get(0);
		service.promise("Spielman", request.getActId(), "Anytime");
		List<ConversationActSummary> ampiesInbox = service.getPendingRequests("Ampie");
		ConversationActSummary promise = ampiesInbox.get(0);
		return promise;
	}

	protected void requestToAssertComplete(ConversationForActionServiceImpl service) {
		requestToStart(service);
		List<TaskSummary> spielmansInbox = getTaskService().getTasksOwned("Spielman", "en-UK");
		TaskSummary task = spielmansInbox.get(0);
		ConversationForAction cfa1 = (ConversationForAction) getTaskService().getTaskById(task.getId());
		// When
		service.assertComplete("Spielman", cfa1.getCurrentAct().getId(),Collections.singletonMap("numberOfWalls", (Object) 9),  "Finished!");
	}

	protected void requestToStart(ConversationForActionServiceImpl service) {
		requestPromiseAndAccept(service);
		ConversationActSummary accept = service.getPendingRequests("Spielman").get(0);
		service.start("Spielman", accept.getActId(), "I'm starting");
	}

	protected void requestPromiseAndAccept(ConversationForActionServiceImpl service) {
		ConversationActSummary promise = requestAndPromise(service);
		service.accept("Ampie", promise.getActId(), "Thanks");
	}

	protected void requestDirectly(ConversationForActionServiceImpl service) {
		ConversationForActionImpl cfa = new ConversationForActionImpl();
		cfa.setName("Build me a house");
		TaskDataImpl taskData = new TaskDataImpl();
		taskData.setDeploymentId((String) getRuntimeEngine().getKieSession().getEnvironment().get("deploymentId"));
		cfa.setTaskData(taskData);
		NegotiationStep step = new NegotiationStep();
		step.setComment("Please");
		step.setDateOfCommencement(Timestamp.valueOf("2014-09-14 14:22:00.0"));
		step.setDateOfCompletion(Timestamp.valueOf("2015-09-14 14:22:00.0"));
		step.setInput(Collections.singletonMap("numberOfBricks", (Object) 4000));
		step.setOutput(Collections.singletonMap("numberOfWalls", (Object) 10));
		// When
		service.requestDirectly("Ampie", "Spielman", cfa, step);
	}
	protected void requestToRenegotiate(ConversationForActionServiceImpl service, String userId, int bricks, int walls) {
		requestToStart(service);
		ConversationForActionSummary cfas = service.getOwnedCommitments("Spielman").get(0);
		ConversationForAction cfa = (ConversationForAction) getTaskService().getTaskById(cfas.getId());
		ConversationAct start = cfa.getCurrentAct();
		NegotiationStep step = new NegotiationStep();
		step.setPreviousActId(start.getId());
		step.setComment("What about " + bricks + " bricks and " + walls + "walls.");
		step.setDateOfCommencement(Timestamp.valueOf("2014-09-15 14:22:00.0"));
		step.setDateOfCompletion(Timestamp.valueOf("2015-09-15 14:22:00.0"));
		step.setInput(Collections.singletonMap("numberOfBricks", (Object) bricks));
		step.setOutput(Collections.singletonMap("numberOfWalls", (Object) walls));
		// When
		service.renegotiate(userId, step);
	}
	protected ConversationForActionServiceImpl createConversationService() {
//		super.addEnvironmentEntry(JpaCaseFileImplementation.CASE_FILE_PERSISTENCE_UNIT_NAME,"org.jbpm.persistence.jpa");
		createRuntimeManager();
		ConversationForActionServiceImpl service = new ConversationForActionServiceImpl();
		service.setTaskService(getTaskService());
		return service;
	}


}