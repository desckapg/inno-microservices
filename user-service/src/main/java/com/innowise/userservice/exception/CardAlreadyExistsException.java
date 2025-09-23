package com.innowise.userservice.exception;

public class CardAlreadyExistsException extends RuntimeException {

  public CardAlreadyExistsException(String number) {
    super("Card with number(" + number + ") also exists");
  }
}
