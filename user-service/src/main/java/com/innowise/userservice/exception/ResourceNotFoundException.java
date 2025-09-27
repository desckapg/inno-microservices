package com.innowise.userservice.exception;

import java.io.Serial;
import lombok.Getter;

@Getter
public class ResourceNotFoundException extends AppRuntimeException {

  @Serial
  private static final long serialVersionUID = -3166837044900038286L;

  private final String resource;
  private final String criteria;

  private ResourceNotFoundException(String message, String resource, String criteria) {
    super(message);
    this.resource = resource;
    this.criteria = criteria;
  }

  public static ResourceNotFoundException byId(String resource, Object id) {
    return new ResourceNotFoundException(resource, "id=" + id,
        resource + " with id(" + id + ") not found");
  }

  public static ResourceNotFoundException byField(String resource, String field, Object value) {
    return new ResourceNotFoundException(resource, field + "=" + value,
        resource + " with " + field + "(" + value + ") not found");
  }
}
