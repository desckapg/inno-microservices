package com.innowise.userservice.exception;

public class UserNotOwnCardException extends RuntimeException {

  public UserNotOwnCardException(Long userId, Long cardId) {
    super("User with id (" + userId + ") does not own card with id (" + cardId + ")");
  }
}
