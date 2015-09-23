package org.pavanecce.cmmn.cfa.api;

import java.util.Date;

import org.jbpm.services.task.impl.model.OrganizationalEntityImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.OrganizationalEntity;

public interface ConversationAct {
	ConversationForAction getConversationForAction();

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

	long getId();

	boolean isDispute();

	String getComment();

	UserImpl getActor();

	OrganizationalEntityImpl getAddressedTo();

	Date getDateOfCompletion();

	Date getDateOfCommencement();

}