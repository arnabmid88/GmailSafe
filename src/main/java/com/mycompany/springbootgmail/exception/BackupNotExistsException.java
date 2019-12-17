package com.mycompany.springbootgmail.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code=HttpStatus.BAD_REQUEST , reason="Backup doesn't exists.")
public class BackupNotExistsException extends Exception{

}