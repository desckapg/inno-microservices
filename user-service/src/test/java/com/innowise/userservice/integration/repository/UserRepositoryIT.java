package com.innowise.userservice.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.JpaRepositoryIT;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.support.TransactionTemplate;

@JpaRepositoryIT
@RequiredArgsConstructor
@TestInstance(Lifecycle.PER_CLASS)
public class UserRepositoryIT extends AbstractIntegrationTest {

  private Long userFixtureId;
  private String userFixtureEmail;

  private final TestEntityManager entityManager;
  private final UserRepository userRepository;
  private final TransactionTemplate transactionTemplate;

  @BeforeAll
  void prepareFixtures() {
    var userFixture = userWithoutCardsBuilder.build();
    transactionTemplate.executeWithoutResult(status ->
        entityManager.persistAndFlush(userFixture));
    userFixtureId = userFixture.getId();
    userFixtureEmail = userFixture.getEmail();
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(status -> {
      var user = entityManager.find(User.class, userFixtureId);
      if (user != null) {
        entityManager.remove(user);
      }
    });
  }

  @Test
  void findByEmail_whenEmailNotExists_shouldReturnEmptyOptional() {
    var nonExistentEmail = "nonexistent@example.com";

    assertThat(userRepository.findByEmail(nonExistentEmail))
        .isEmpty();
  }

  @Test
  void findByEmail_whenEmailExists_shouldReturnUserWithEmail() {
    assertThat(userRepository.findByEmail(userFixtureEmail))
        .isPresent()
        .hasValueSatisfying(user -> {
          assertThat(user.getEmail()).isEqualTo(userFixtureEmail);
          assertThat(user.getId()).isEqualTo(userFixtureId);
        });
  }

  @Test
  void findAllByIdIn_whenIdsExist_shouldReturnMatchingUsers() {
    assertThat(userRepository.findAllByIdIn(List.of(userFixtureId)))
        .hasSize(1)
        .extracting(User::getId)
        .containsExactly(userFixtureId);
  }

  @Test
  void findAllByIdIn_whenIdsNotExist_shouldReturnEmptyList() {
    var nonExistentIds = List.of(999L, 1000L);

    assertThat(userRepository.findAllByIdIn(nonExistentIds))
        .isEmpty();
  }

  @Test
  void findAllByIdIn_whenEmptyIdsList_shouldReturnEmptyList() {
    assertThat(userRepository.findAllByIdIn(List.of()))
        .isEmpty();
  }

  @Test
  void findAllByIdIn_whenMixedIds_shouldReturnOnlyExistingUsers() {
    var mixedIds = List.of(userFixtureId, 999L);

    assertThat(userRepository.findAllByIdIn(mixedIds))
        .hasSize(1)
        .extracting(User::getId)
        .containsExactlyInAnyOrder(userFixtureId);

  }
}
