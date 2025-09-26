package com.innowise.userservice.exception;

import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends AppRuntimeException {

  private final String resource;
  private final String criteria;

  private ResourceAlreadyExistsException(String resource, String criteria, String message) {
    super(message);
    this.resource = resource;
    this.criteria = criteria;
  }

  public static ResourceAlreadyExistsException byField(
      String resource,
      String field,
      Object value) {
    return new ResourceAlreadyExistsException(resource, field + "=" + value,
        "%s with %s(%s) also exists".formatted(resource, field, value));
  }
}
