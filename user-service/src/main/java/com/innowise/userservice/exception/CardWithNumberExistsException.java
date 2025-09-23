package com.innowise.userservice.exception;

public class CardWithNumberExistsException extends RuntimeException {

  public CardWithNumberExistsException(String number) {
    super("Card with number(" + number + ") also exists");
  }
}
