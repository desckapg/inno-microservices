package com.innowise.userservice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@JsonInclude(Include.NON_NULL)
@Builder
public record ErrorDto(
    OffsetDateTime timestamp,
    int httpStatus,
    HttpStatus code,
    String message,
    String path,
    String field,
    String fieldViolation
) implements Serializable {

  public static ErrorDto validation(String field, String fieldViolation) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .httpStatus(400)
        .code(HttpStatus.UNPROCESSABLE_CONTENT)
        .field(field)
        .fieldViolation(fieldViolation)
        .build();
  }

  public static ErrorDto notFound(String message, String path) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .httpStatus(404)
        .code(HttpStatus.NOT_FOUND)
        .message(message)
        .path(path)
        .build();
  }

  public static ErrorDto alreadyExists(String message, String path) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .httpStatus(409)
        .code(HttpStatus.CONFLICT)
        .message(message)
        .path(path)
        .build();
  }

  public static ErrorDto badRequest(String message, String path) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .httpStatus(400)
        .code(HttpStatus.BAD_REQUEST)
        .message(message)
        .path(path)
        .build();
  }

  public static ErrorDto internal(String path) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .httpStatus(500)
        .code(HttpStatus.INTERNAL_SERVER_ERROR)
        .message("Internal server error")
        .path(path)
        .build();
  }
}
