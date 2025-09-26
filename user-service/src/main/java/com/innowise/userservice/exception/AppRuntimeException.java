package com.innowise.userservice.exception;

public abstract class AppRuntimeException extends RuntimeException {

  protected AppRuntimeException(String message) {
    super(message);
  }

  // Due to the getMessage() method in Throwable is nullable
  @Override
  public String getMessage() {
    return super.getMessage();
  }
}
