package com.mycompany.springbootgmail.service;

import com.mycompany.springbootgmail.dto.RetrieveBackupDto;
import com.mycompany.springbootgmail.exception.FilePermissionException;

import java.io.IOException;
import java.util.List;

public interface GmailBackupService {

	void startCreatingBackup(String backupId) throws IOException, FilePermissionException;
	List<RetrieveBackupDto> retrieveBackupStatus() throws FilePermissionException;

}
