package org.pavanecce.cmmn.cfa.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class NegotiationStep implements Serializable {

	private static final long serialVersionUID = 1123151515151L;
	private long conversationId;
	private long previousActId;
	private Date dateOfCommencement;
	private Date dateOfCompletion;
	private Map<String,Object> input;
	private Map<String,Object> output;
	private Map<String,Object> fault;
	private String comment;
	public long getConversationId() {
		return conversationId;
	}
	public void setConversationId(long conversationId) {
		this.conversationId = conversationId;
	}
	public Date getDateOfCommencement() {
		return dateOfCommencement;
	}
	public void setDateOfCommencement(Date dateOfCommencement) {
		this.dateOfCommencement = dateOfCommencement;
	}
	public Date getDateOfCompletion() {
		return dateOfCompletion;
	}
	public void setDateOfCompletion(Date dateOfCompletion) {
		this.dateOfCompletion = dateOfCompletion;
	}
	public Map<String, Object> getInput() {
		return input;
	}
	public void setInput(Map<String, Object> input) {
		this.input = input;
	}
	public Map<String, Object> getOutput() {
		return output;
	}
	public void setOutput(Map<String, Object> output) {
		this.output = output;
	}
	public Map<String, Object> getFault() {
		return fault;
	}
	public void setFault(Map<String, Object> fault) {
		this.fault = fault;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public long getPreviousActId() {
		return previousActId;
	}
	public void setPreviousActId(long previousActId) {
		this.previousActId = previousActId;
	}
	
}
