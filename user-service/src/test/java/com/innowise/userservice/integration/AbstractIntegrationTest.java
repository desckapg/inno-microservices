package com.innowise.userservice.integration;

import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import java.time.LocalDate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

public abstract class AbstractIntegrationTest {

  static PostgreSQLContainer <?> postgres =
      new PostgreSQLContainer<>("postgres:latest");

  static {
    Startables.deepStart(postgres).join();
  }

  protected static User.UserBuilder userWithoutCardsBuilder = User.builder()
      .name("John")
      .surname("Doe")
      .birthDate(LocalDate.now())
      .email("johndoe@example.com");

  protected static Card.CardBuilder cardWithoutUserBuilder = Card.builder()
      .number("1234-5678-9101-1121")
      .holder("Some Bank")
      .expirationDate(LocalDate.now().plusDays(1L));

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

}
