package com.mycompany.springbootgmail.dto;

public class RetrieveBackupDto {

	private String backupId;
	private String dateOfCreation;
	private String status;
	public String getBackupId() {
		return backupId;
	}
	public void setBackupId(String backupId) {
		this.backupId = backupId;
	}
	public String getDateOfCreation() {
		return dateOfCreation;
	}
	public void setDateOfCreation(String dateOfCreation) {
		this.dateOfCreation = dateOfCreation;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
