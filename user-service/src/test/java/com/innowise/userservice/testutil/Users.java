package com.innowise.userservice.testutil;

import com.innowise.userservice.model.entity.User;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

public class Users {

  private static final AtomicLong SEQ = new AtomicLong(1);

  private Users() {
  }

  public static User.UserBuilder user(String name, String surname, String email) {
    return User.builder()
        .name(name)
        .surname(surname)
        .email(email)
        .birthDate(LocalDate.of(1990, 1, 1));
  }

  public static User build() {
    long n = SEQ.getAndIncrement();
    var user = User.builder()
        .name("John" + n)
        .surname("Doe" + n)
        .birthDate(LocalDate.now().minusYears(20))
        .email("john" + n + "@example.com")
        .build();
    user.setId(n);
    return user;
  }

  public static User buildWithoutId() {
    long n = SEQ.getAndIncrement();
    return User.builder()
        .name("John" + n)
        .surname("Doe" + n)
        .birthDate(LocalDate.now().minusYears(20))
        .email("john" + n + "@example.com")
        .build();
  }

  public static User build(String name, String surname, String email) {
    return user(name, surname, email).build();
  }

  public static User buildWithId(Long id, String name, String surname, String email) {
    var user = build(name, surname, email);
    user.setId(id);
    return user;
  }

  public static User buildWithId(Long id) {
    var user = build();
    user.setId(id);
    return user;
  }
}
