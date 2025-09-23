package com.innowise.userservice.handler;

import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {RuntimeException.class})
  public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
    return ResponseEntity.internalServerError().body(ex.getMessage());
  }

  @ExceptionHandler(value = {UserNotFoundException.class, CardNotFoundException.class})
  public ResponseEntity<String> handleNotFoundException(RuntimeException ex) {
    return ResponseEntity.notFound().build();
  }

}
