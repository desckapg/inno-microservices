package com.innowise.userservice.testutil;

import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;

public final class Fixtures {

  private Fixtures() {
  }

  public static User userWithCard() {
    User user = Users.build();
    Card card = Cards.card().user(user).build();
    user.getCards().add(card);
    return user;
  }
}
