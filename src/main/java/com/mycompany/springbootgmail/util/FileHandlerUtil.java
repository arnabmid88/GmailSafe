package com.mycompany.springbootgmail.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.mycompany.springbootgmail.exception.BackupNotExistsException;

public class FileHandlerUtil {
	
	private final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	private final String DIRECTORY_NAME = "GmailSafe-Backup";
	private final String ZIP_DIRECTORY_NAME = "GmailSafe-Backup-Compressed";
	private final String STATUS_FILE_NAME = "BackupStatus";
	private String backupId;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean createBackupDirectory(){
		File file = new File(TEMP_DIR+DIRECTORY_NAME+File.separator+this.getBackupId());
		
        if (!file.exists()) {
            return file.mkdirs();
        }
		return true;
	}
	
	public boolean createLabelDirectory(String label){
		File file = new File(TEMP_DIR+DIRECTORY_NAME+File.separator+this.getBackupId()+File.separator+label);
        if (!file.exists()) {
            return file.mkdir();
        }
		return true;
	}
	
	public void createMessageFileInDirectory(String label, String data, String name){
		String extension = ".txt";
		File file = new File(TEMP_DIR+DIRECTORY_NAME+File.separator+this.getBackupId()+File.separator+label+File.separator+name+extension);
		
		try {
			//file.createNewFile();
			FileWriter fw = new FileWriter(file);
	        fw.write(data);
	        fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public boolean createStatusFileInDirectory(){
		String extension = ".csv";
		File file = new File(TEMP_DIR+DIRECTORY_NAME+File.separator+STATUS_FILE_NAME+extension);
		try {
			if(!file.exists())
				return file.createNewFile();
			return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	public void  updateStatusFile(String status, boolean isNew) {
		String extension = ".csv";
		List<String> lines = new ArrayList<String>();
	    String line = null;
        try {
            File f1 = new File(TEMP_DIR+DIRECTORY_NAME+File.separator+STATUS_FILE_NAME+extension);
            FileReader fr = new FileReader(f1);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if (!isNew && line.contains(this.backupId)){
                	String[] data = line.split(",");
                	data[2] = status;
                	line = convertToCSV(data);
                }
                    
                lines.add(line);
            }
            fr.close();
            br.close();

            FileWriter fw = new FileWriter(f1);
            BufferedWriter out = new BufferedWriter(fw);
            if(isNew){
            	String[] data = {this.backupId,new SimpleDateFormat("dd-MM-yyyy").format(new Date()),status};
            	line = convertToCSV(data);
            	lines.add(line);
            }
            for(String s : lines){
            	out.write(s);
            	out.newLine();
            }
                 
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	public List<String>  readStatusFile() throws IOException {
		String extension = ".csv";
		List<String> lines = new ArrayList<String>();
	    String line = null, returnValue=null;
        try {
            File f1 = new File(TEMP_DIR+DIRECTORY_NAME+File.separator+STATUS_FILE_NAME+extension);
            FileReader fr = new FileReader(f1);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                lines.add(line);

            }
            fr.close();
            br.close();
            return lines;
        }catch(IOException ioe){
        	throw ioe;
        }catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
	
	public String  getStatusById() throws Exception {
		String extension = ".csv";
	    String line = null;
        try {
            File f1 = new File(TEMP_DIR+DIRECTORY_NAME+File.separator+STATUS_FILE_NAME+extension);
            FileReader fr = new FileReader(f1);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if(line.contains(this.backupId)){
                	return line.split(",")[2];
                }

            }
            fr.close();
            br.close();
            throw new BackupNotExistsException();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

	public String convertToCSV(String[] data) {
        return Stream.of(data)
            .map(this::escapeSpecialCharacters)
            .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
    
    public Resource prepareZipToTransfer(boolean isLabel, String labelName) throws IOException{
    	File fileComp = new File(TEMP_DIR+ZIP_DIRECTORY_NAME);
    	if(!fileComp.exists())
    		fileComp.mkdir();
    	String sourceFile = TEMP_DIR+DIRECTORY_NAME+File.separator+this.getBackupId();
    	String outputFilename = TEMP_DIR+ZIP_DIRECTORY_NAME+File.separator+this.getBackupId()+".zip";
    	if(isLabel){
    		sourceFile = TEMP_DIR+DIRECTORY_NAME+File.separator+this.getBackupId()+File.separator+labelName;
    		outputFilename = TEMP_DIR+ZIP_DIRECTORY_NAME+File.separator+this.getBackupId()+"_"+labelName+".zip";
    	}
        final FileOutputStream fos = new FileOutputStream(outputFilename);
        final ZipOutputStream zipOut = new ZipOutputStream(fos);
        final File fileToZip = new File(sourceFile);
        try {
			zipFile(fileToZip, fileToZip.getName(), zipOut);
			zipOut.close();
	        fos.close();
	        return loadAsResource(outputFilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        
    }
    
    public Resource loadAsResource(String filename) {
		try {
			Path file = Paths.get(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("Could not read file: " + filename);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not read file: " + filename, e);
		}
	}
    
    private void zipFile(final File fileToZip, final String fileName, final ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            final File[] children = fileToZip.listFiles();
            for (final File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        final FileInputStream fis = new FileInputStream(fileToZip);
        final ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        final byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }


	public String getBackupId() {
		return backupId;
	}



	public void setBackupId(String backupId) {
		this.backupId = backupId;
	}

}
