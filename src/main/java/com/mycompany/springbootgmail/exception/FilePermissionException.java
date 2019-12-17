package com.mycompany.springbootgmail.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code=HttpStatus.FORBIDDEN , reason="User doesn't have permission to create file or folder!")
public class FilePermissionException extends Exception{

}
