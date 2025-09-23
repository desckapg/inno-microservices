package com.innowise.userservice.exception;

public class UserWithEmailExistsException extends RuntimeException {

  public UserWithEmailExistsException(String email) {
    super("User with email(" + email + ") also exists");
  }
}
