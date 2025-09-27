package com.innowise.userservice.exception;

import java.io.Serializable;

public abstract class AppRuntimeException extends RuntimeException implements Serializable {

  protected AppRuntimeException(String message) {
    super(message);
  }

}
