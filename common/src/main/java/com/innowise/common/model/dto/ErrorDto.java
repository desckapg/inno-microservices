package com.innowise.common.model.dto;

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
    int status,
    String detail,
    String instance,
    String title,
    String field,
    String fieldViolation
) implements Serializable {

  public static ErrorDto validation(String field, String fieldViolation) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
        .title(HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase())
        .field(field)
        .fieldViolation(fieldViolation)
        .build();
  }

  public static ErrorDto notFound(String detail, String instance) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .title(HttpStatus.NOT_FOUND.getReasonPhrase())
        .detail(detail)
        .instance(instance)
        .build();
  }

  public static ErrorDto alreadyExists(String detail, String path) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .status(HttpStatus.CONFLICT.value())
        .title(HttpStatus.CONTINUE.getReasonPhrase())
        .detail(detail)
        .instance(path)
        .build();
  }

  public static ErrorDto authFailed(String detail) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .status(HttpStatus.UNAUTHORIZED.value())
        .title(HttpStatus.UNAUTHORIZED.getReasonPhrase())
        .detail(detail)
        .build();
  }

  public static ErrorDto forbidden(String detail) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .status(HttpStatus.FORBIDDEN.value())
        .title(HttpStatus.FORBIDDEN.getReasonPhrase())
        .detail(detail)
        .build();
  }

  public static ErrorDto internal(String path) {
    return builder()
        .timestamp(OffsetDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .title(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .instance(path)
        .build();
  }
}
