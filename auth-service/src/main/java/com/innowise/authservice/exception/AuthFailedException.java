package com.innowise.authservice.exception;

import java.io.Serial;

public class AuthFailedException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 7309000673740782570L;

  public AuthFailedException() {
    super("Incorrect login or password");
  }

}
