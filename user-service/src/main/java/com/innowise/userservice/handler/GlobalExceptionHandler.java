package com.innowise.userservice.handler;

import com.innowise.userservice.exception.CardAlreadyExistsException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {RuntimeException.class})
  public ResponseEntity<Void> handleRuntimeException(RuntimeException ex) {
    log.error(ex.getMessage(), ex);
    return ResponseEntity.internalServerError().build();
  }

  @ExceptionHandler(value = {UserNotFoundException.class, CardNotFoundException.class})
  public ResponseEntity<Void> handleNotFoundException(RuntimeException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(value = {UserAlreadyExistsException.class, CardAlreadyExistsException.class})
  public ResponseEntity<String> handleAlreadyExistsExceptions(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ex.getMessage());
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return ResponseEntity.unprocessableContent()
        .body(ex.getFieldErrors()
            .stream()
            .map(error -> error.getField() + " : " + error.getDefaultMessage())
            .toList()
        );
  }

}
