package com.innowise.userservice.exception;

public class CardNotFoundException extends RuntimeException {

  public CardNotFoundException(Long id) {
    super("Card with id(" + id + ") not found");
  }
}
