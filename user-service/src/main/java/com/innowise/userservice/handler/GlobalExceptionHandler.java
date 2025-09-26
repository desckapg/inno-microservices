package com.innowise.userservice.handler;

import com.innowise.userservice.exception.ResourceAlreadyExistsException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.exception.UserNotOwnCardException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {RuntimeException.class})
  public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
    log.error(ex.getMessage(), ex);
    return ResponseEntity.internalServerError().build();
  }

  @ExceptionHandler(value = {ResourceNotFoundException.class})
  public ResponseEntity<?> handleNotFoundException(ResourceNotFoundException ex) {
    return ResponseEntity.notFound()
        .build();
  }

  @ExceptionHandler(value = {ResourceAlreadyExistsException.class})
  public ResponseEntity<String> handleAlreadyExistsExceptions(
      ResourceAlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ex.getMessage());
  }

  @ExceptionHandler(value = {UserNotOwnCardException.class})
  public ResponseEntity<?> handleNotOwnCardException(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @Override
  protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    var errors = ex.getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .toList();
    return ResponseEntity.unprocessableContent()
        .body(errors);
  }
}
