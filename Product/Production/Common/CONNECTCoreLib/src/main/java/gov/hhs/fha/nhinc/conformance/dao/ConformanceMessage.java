package gov.hhs.fha.nhinc.conformance.dao;

import java.sql.Blob;
import java.util.Date;

public class ConformanceMessage {

	private int id = 0;
	private String action = null;
	private String direction = null;
	private String messageID = null;
	private String relatesToID = null;
	private Blob message = null;
	private Date confTime = null;
	
	public String getDirection() {
		return direction;
	}
	
	public int getId() {
		return id;
	}
	public String getAction() {
		return action;
	}
	public String getMessageID() {
		return messageID;
	}
	public String getRelatesToID() {
		return relatesToID;
	}
	public Blob getMessage() {
		return message;
	}
	public Date getConfTime() {
		return confTime;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}
	public void setRelatesToID(String relatesToID) {
		this.relatesToID = relatesToID;
	}
	public void setMessage(Blob message) {
		this.message = message;
	}
	public void setConfTime(Date confTime) {
		this.confTime = confTime;
	}
	
	public void setDirection(String direction) {
		this.direction = direction;
	}
}
