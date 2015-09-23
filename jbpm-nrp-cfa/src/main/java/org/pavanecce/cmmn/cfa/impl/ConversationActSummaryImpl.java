package org.pavanecce.cmmn.cfa.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.pavanecce.cmmn.cfa.api.ConversationActKind;
import org.pavanecce.cmmn.cfa.api.ConversationActSummary;
import org.pavanecce.cmmn.cfa.api.ConversationForActionState;

public class ConversationActSummaryImpl implements ConversationActSummary {

	private String name;

	private String statusId;

	private Integer priority;

	private Long id;

	private OrganizationalEntity owner;

	private OrganizationalEntity initiator;

	private long faultContentId;

	private long outputContentId;

	private long inputContentId;

	private OrganizationalEntity renegotiator;

	private String faultType;

	private String faultName;

	private boolean isCommitted;

	private ConversationActKind kind;

	private ConversationForActionState resultingConversationState;

	private Long parentId;

	private String deploymentId;

	private Long processInstanceId;

	private String processId;

	private Date expirationTime;

	private Date activationTime;

	private Date createdOn;

	private Status status;

	private String description;

	private long actId;

	private Date dateOfCommencement;

	private Date dateOfCompletion;

	public ConversationActSummaryImpl(long id, String name, String description, Status status, int priority, OrganizationalEntity actualOwner,
			OrganizationalEntity createdBy, Date createdOn, Date activationTime, Date expirationTime, String processId, long processInstanceId,
			long parentId, String deploymentId, long inputContentId, long outputContentId, long faultContentId, ConversationActKind kind,
			ConversationForActionState resultingConversationState, OrganizationalEntity renegotiator, long actId, Date dateOfCommencement,
			Date dateOfCompletion) {
		this.id = id;
		this.processInstanceId = processInstanceId;
		this.name = name;
		this.description = description;
		this.status = status;
		if (status != null) {
			this.statusId = status.name();
		}
		this.priority = priority;
		this.owner = actualOwner;
		this.initiator = createdBy;
		this.createdOn = createdOn;
		this.activationTime = activationTime;
		this.expirationTime = expirationTime;
		this.processId = processId;
		this.parentId = parentId;
		this.deploymentId = deploymentId;
		this.inputContentId = inputContentId;
		this.outputContentId = outputContentId;
		this.faultContentId = faultContentId;
		this.kind = kind;
		this.resultingConversationState = resultingConversationState;
		this.renegotiator = renegotiator;
		this.actId = actId;
		this.dateOfCommencement = dateOfCommencement;
		this.dateOfCompletion = dateOfCompletion;
	}

	@Override
	public long getConversationId() {
		return id;
	}

	public long getActId() {
		return actId;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getStatusId() {
		return this.statusId;
	}

	@Override
	public Integer getPriority() {
		return this.priority;
	}

	@Override
	public String getActualOwnerId() {
		return owner.getId();
	}

	@Override
	public String getCreatedById() {
		return this.initiator.getId();
	}

	@Override
	public Date getCreatedOn() {
		return this.createdOn;
	}

	@Override
	public Date getActivationTime() {
		return this.activationTime;
	}

	@Override
	public Date getExpirationTime() {
		return this.expirationTime;
	}

	@Override
	public String getProcessId() {
		return this.processId;
	}

	@Override
	public Long getProcessInstanceId() {
		return this.processInstanceId;
	}

	@Override
	public String getDeploymentId() {
		return this.deploymentId;
	}

	@Override
	public Long getParentId() {
		return this.parentId;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public ConversationActKind getKind() {
		return this.kind;
	}

	@Override
	public ConversationForActionState getResultingConversationState() {
		return this.resultingConversationState;
	}

	@Override
	public boolean isCommitted() {
		return this.isCommitted;
	}

	@Override
	public String getFaultName() {
		return this.faultName;
	}

	@Override
	public String getFaultType() {
		return this.faultType;
	}

	@Override
	public OrganizationalEntity getRenegotiator() {
		return this.renegotiator;
	}

	@Override
	public long getInputContentId() {
		return this.inputContentId;
	}

	@Override
	public long getOutputContentId() {
		return this.outputContentId;
	}

	@Override
	public long getFaultContentId() {
		return this.faultContentId;
	}

	@Override
	public OrganizationalEntity getOwner() {
		return this.owner;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	public String getDescription() {
		return description;
	}

	public Date getDateOfCommencement() {
		return dateOfCommencement;
	}

	public Date getDateOfCompletion() {
		return dateOfCompletion;
	}
}
