package com.innowise.userservice.exception;

public abstract class AppRuntimeException extends RuntimeException {

  protected AppRuntimeException(String message) {
    super(message);
  }

}
