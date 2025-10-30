package com.innowise.orderservice.exception;

import com.innowise.common.exception.ExternalApiException;
import com.innowise.common.exception.ResourceAlreadyExistsException;
import com.innowise.common.exception.ResourceNotFoundException;
import com.innowise.common.model.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@NullMarked
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Map<String, Integer> CONSTRAINT_PRIORITY = Map.of(
      "NotNull", 1,
      "NotBlank", 1,
      "Size", 2,
      "Pattern", 2,
      "Email", 2
  );

  @ExceptionHandler(value = {RuntimeException.class})
  public ResponseEntity<ErrorDto> handleRuntimeException(RuntimeException ex,
      HttpServletRequest request) {
    log.error(ex.getMessage(), ex);
    return ResponseEntity.internalServerError()
        .body(ErrorDto.internal(request.getRequestURI()));
  }

  @ExceptionHandler(value = {ResourceNotFoundException.class})
  public ResponseEntity<ErrorDto> handleNotFoundException(ResourceNotFoundException ex,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorDto.notFound(ex.getMessage(), request.getRequestURI()));
  }

  @ExceptionHandler(value = {ResourceAlreadyExistsException.class})
  public ResponseEntity<ErrorDto> handleAlreadyExistsException(
      ResourceAlreadyExistsException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorDto.alreadyExists(ex.getMessage(), request.getRequestURI()));
  }

  @ExceptionHandler(value = {ExternalApiException.class})
  public ResponseEntity<ErrorDto> handleExternalApiException(
      ExternalApiException ex,
      HttpServletRequest request
  ) {
    return ResponseEntity.internalServerError()
        .body(ErrorDto.internal(request.getRequestURI()));
  }

  @ExceptionHandler(value = {AuthorizationDeniedException.class})
  public ResponseEntity<ErrorDto> handleAuthorizationDeniedException(
      AuthorizationDeniedException ex,
      HttpServletRequest request
  ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
    FieldError selected = fieldErrors.stream()
        .min(Comparator.comparingInt(this::priority))
        .orElse(fieldErrors.getFirst());

    return ResponseEntity.unprocessableContent().body(
        ErrorDto.validation(
            selected.getField(),
            selected.getDefaultMessage()
        )
    );
  }

  private int priority(FieldError fe) {
    String[] codes = fe.getCodes();
    for (String raw : codes) {
      String simple = raw.contains(".") ? raw.substring(0, raw.indexOf('.')) : raw;
      Integer p = CONSTRAINT_PRIORITY.get(simple);
      if (p != null) {
        return p;
      }
    }
    return 0;
  }

}
