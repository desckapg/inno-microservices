package com.innowise.userservice.exception;

public class UserAlreadyExistsException extends RuntimeException {

  public UserAlreadyExistsException(String email) {
    super("User with email(" + email + ") also exists");
  }
}
