package com.mycompany.springbootgmail.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.mycompany.springbootgmail.dto.RetrieveBackupDto;
import com.mycompany.springbootgmail.exception.BackupNotExistsException;
import com.mycompany.springbootgmail.exception.FilePermissionException;
import com.mycompany.springbootgmail.util.FileHandlerUtil;

@Service
public class GmailBackupServiceImpl implements GmailBackupService{
	
	private GmailLabelService gmailLabelService;
	private GmailMessageService gmailMessageService;
	
	public GmailBackupServiceImpl(GmailMessageService gmailMessageService,GmailLabelService gmailLabelService){
		this.gmailLabelService = gmailLabelService;
		this.gmailMessageService = gmailMessageService;
	}
	
	@Async
	public void startCreatingBackup(String backupId) throws IOException, FilePermissionException{
		FileHandlerUtil fileUtil = new FileHandlerUtil();
		fileUtil.setBackupId(backupId);
		try{
		if(fileUtil.createBackupDirectory() && fileUtil.createStatusFileInDirectory()){
			fileUtil.updateStatusFile("In Progress", true);
			List<Label> labels = gmailLabelService.getLabels();
			
			for(Label label : labels){
				if(fileUtil.createLabelDirectory(label.getName())){
					List<String> labelList = new ArrayList<String>();
					labelList.add(label.getId());
					List<Message> messageList = gmailMessageService.getMessages(null, labelList);
					
					for(Message message: messageList){
						Message actualMessage = gmailMessageService.getMessage(message.getId());
						List<MessagePart> partList = actualMessage.getPayload().getParts();
						if(partList !=null && partList.size()>0){
							String messageBody = StringUtils.newStringUtf8(Base64.decodeBase64(actualMessage.getPayload().getParts().get(0).getBody().getData()));
							fileUtil.createMessageFileInDirectory(label.getName(), messageBody, message.getId());
						}
						
						
					}
					
				}
				
			}
			
			//fileUtil.prepareZipToTransfer(false, null);
			fileUtil.updateStatusFile("Ok", false);
		}
		}catch(IOException ioe){
			throw new FilePermissionException();
		}
		catch(Exception ex){
			ex.printStackTrace();
			fileUtil.updateStatusFile("Failed", false);
		}
	}
	
	public List<RetrieveBackupDto> retrieveBackupStatus() throws FilePermissionException{
		
		List<RetrieveBackupDto> allStatus = new ArrayList<>();
		FileHandlerUtil fileUtil = new FileHandlerUtil();
		try{
			List<String> statusLines = fileUtil.readStatusFile();
			
			if(statusLines !=null && statusLines.size()>0){
				
				for(String statusLine: statusLines){
					String[] statusData = statusLine.split(",");
					RetrieveBackupDto dto = new RetrieveBackupDto();
					dto.setBackupId(statusData[0]);
					dto.setDateOfCreation(statusData[1]);
					dto.setStatus(statusData[2]);
					allStatus.add(dto);
				}
			}
			
			
			return allStatus;
		}catch(IOException ioe){
			throw new FilePermissionException();
		}
		catch(Exception ex){
			ex.printStackTrace();
			fileUtil.updateStatusFile("Failed", false);
			throw ex;
		}
		
		
	}

}
