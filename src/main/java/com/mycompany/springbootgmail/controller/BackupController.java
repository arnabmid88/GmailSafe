package com.mycompany.springbootgmail.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.springbootgmail.dto.CreateBackupDto;
import com.mycompany.springbootgmail.dto.RetrieveBackupDto;
import com.mycompany.springbootgmail.exception.BackupNotExistsException;
import com.mycompany.springbootgmail.exception.FilePermissionException;
import com.mycompany.springbootgmail.service.GmailBackupService;
import com.mycompany.springbootgmail.util.FileHandlerUtil;

@RestController
@RequestMapping("/api/v1/backups")
public class BackupController {

	private final GmailBackupService gmailBackupService;
    public BackupController(GmailBackupService gmailBackupService) {
        this.gmailBackupService = gmailBackupService;
    }
    
    @PostMapping
    public CreateBackupDto createBackup() throws IOException,FilePermissionException {
    	CreateBackupDto dto = new CreateBackupDto();
    	String ts = String.valueOf(System.currentTimeMillis());
    	dto.setBackupId(ts);
    	try{
    		gmailBackupService.startCreatingBackup(ts);
            return dto;
    	}catch(FilePermissionException be){
			throw new FilePermissionException();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
    	
    }
    
    @GetMapping
    public List<RetrieveBackupDto> retrieveBackup() throws IOException, FilePermissionException {    	
    	try {
			return gmailBackupService.retrieveBackupStatus();
		} catch (FilePermissionException e) {
			// TODO Auto-generated catch block
			throw new FilePermissionException();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
    }
    
    @GetMapping(path="/exports/{backupId}")
    public ResponseEntity<Resource> retrieveBackupById(@PathVariable("backupId") String backupId ) throws IOException, BackupNotExistsException {
    	ResponseEntity responseEntity;
    	try{
    		FileHandlerUtil fileUtil = new FileHandlerUtil();
            fileUtil.setBackupId(backupId);
            String status = fileUtil.getStatusById();
            if("Ok".equalsIgnoreCase(status)){
            	Resource file = fileUtil.prepareZipToTransfer(false, null);
            	return ResponseEntity.ok()
        				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
        				.body(file);
            }else{
            	responseEntity = new ResponseEntity("Backup Work in progress", HttpStatus.INTERNAL_SERVER_ERROR);
				return responseEntity;
            }
    	}catch(BackupNotExistsException be){
			throw new BackupNotExistsException();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseEntity = new ResponseEntity("Error!!! Try after some time", HttpStatus.INTERNAL_SERVER_ERROR);
			return responseEntity;
		}
		
        
    }
    
    @GetMapping(path="/exports/{backupId}/{label}")
    
    public ResponseEntity<Resource> retrieveBackupByLabel(@PathVariable("backupId") String backupId, @PathVariable("label") String label) throws IOException, BackupNotExistsException {
    	ResponseEntity responseEntity;
    	try{
    		FileHandlerUtil fileUtil = new FileHandlerUtil();
            fileUtil.setBackupId(backupId);
            String status = fileUtil.getStatusById();
            if("Ok".equalsIgnoreCase(status)){
            	Resource file = fileUtil.prepareZipToTransfer(true, label);
            	return ResponseEntity.ok()
        				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
        				.body(file);
            }else{
            	responseEntity = new ResponseEntity("Backup Work in progress", HttpStatus.INTERNAL_SERVER_ERROR);
				return responseEntity;
            }
    	}catch(BackupNotExistsException be){
			throw new BackupNotExistsException();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseEntity = new ResponseEntity("Error!!! Try after some time", HttpStatus.INTERNAL_SERVER_ERROR);
			return responseEntity;
		}
    	
    }
}
