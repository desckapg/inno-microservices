package com.innowise.common.exception;

import java.io.Serial;

public class ExternalApiException extends RuntimeException {


  @Serial
  private static final long serialVersionUID = -2076544813962210621L;

  public ExternalApiException(String message) {
    super(message);
  }
}
