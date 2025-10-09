package com.innowise.userservice.testutil;

import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class Cards {

  private static final AtomicLong SEQ = new AtomicLong(1L);

  private Cards() {
  }

  public static Card.CardBuilder card() {
    return Card.builder().number(randomNumber()).holder("Some Bank")
        .expirationDate(LocalDate.now().plusYears(2));
  }

  public static Card build() {
    return card().build();
  }

  public static Card build(Long id, User user) {
    var card = card().build();
    card.setId(id);
    card.setUser(user);
    user.addCard(card);
    return card;
  }

  public static Card build(User user) {
    var card = card().build();
    user.addCard(card);
    card.setId(SEQ.getAndIncrement());
    card.setUser(user);
    return card;
  }

  public static Card buildWithoutId(User user) {
    var card = card().build();
    user.addCard(card);
    return card;
  }

  public static Card build(Long id) {
    var card = card().build();
    card.setId(id);
    return card;
  }

  public static Card build(Long id, String number, String holder, User user) {
    var card = Card.builder()
        .user(user)
        .number(number)
        .holder(holder)
        .expirationDate(LocalDate.now().plusYears(2))
        .build();
    card.setId(id);
    return card;
  }

  private static String randomNumber() {
    ThreadLocalRandom r = ThreadLocalRandom.current();
    return String.format("%04d-%04d-%04d-%04d", r.nextInt(10000), r.nextInt(10000),
        r.nextInt(10000), r.nextInt(10000));
  }
}
