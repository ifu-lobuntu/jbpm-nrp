package org.pavanecce.cmmn.cfa.api;

import java.util.Date;

import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.QuickTaskSummary;

public interface ConversationActSummary extends QuickTaskSummary {
	long getConversationId();

	ConversationActKind getKind();

	ConversationForActionState getResultingConversationState();

	boolean isCommitted();

	String getFaultName();

	String getFaultType();

	OrganizationalEntity getRenegotiator();

	long getInputContentId();

	long getOutputContentId();

	long getFaultContentId();

	OrganizationalEntity getOwner();

	Date getDateOfCommencement();

	Date getDateOfCompletion();

	long getActId();
}
