package com.innowise.userservice.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.IT;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.transaction.support.TransactionTemplate;

@IT
@RequiredArgsConstructor
@TestInstance(Lifecycle.PER_CLASS)
class UserRepositoryIT extends AbstractIntegrationTest {

  private User userFixture;
  private Card cardFixture;

  private final TestEntityManager entityManager;
  private final UserRepository userRepository;
  private final TransactionTemplate transactionTemplate;

  @BeforeAll
  void prepareFixtures() {
    userFixture = Users.buildWithoutId();
    cardFixture = Cards.buildWithoutId(userFixture);
    transactionTemplate.executeWithoutResult(_ -> entityManager.persistAndFlush(userFixture));
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(_ -> {
      var user = entityManager.find(User.class, userFixture.getId());
      entityManager.remove(Objects.requireNonNull(user));
    });
  }

  @Test
  void findWithCardsById_whenUserExists_shouldReturnUserWithCards() {
    assertThat(userRepository.findWithCardsById(userFixture.getId()))
        .hasValueSatisfying(user -> {
          assertThat(user).isEqualTo(userFixture);
          assertThat(Hibernate.isInitialized(user.getCards()))
              .isTrue();
          assertThat(user.getCards())
              .containsExactly(cardFixture);
        });
  }

  @Test
  void findAllByIdIn_whenUsersExist_shouldReturnUsers() {
    assertThat(userRepository.findAllByIdIn(List.of(userFixture.getId())))
        .containsExactly(userFixture)
        .satisfiesExactly(user ->
            assertThat(Hibernate.isInitialized(user.getCards())
            ).isTrue());
  }

  @Test
  void findUserWithCardsById_whenUserNotExists_shouldEmptyOptional() {
    assertThat(userRepository.findWithCardsById(Long.MAX_VALUE))
        .isEmpty();
  }

  @Test
  void findByEmail_whenEmailNotExists_shouldReturnEmptyOptional() {
    var nonExistentEmail = "nonexistent@example.com";

    assertThat(userRepository.findByEmail(nonExistentEmail))
        .isEmpty();
  }

  @Test
  void findByEmail_whenEmailExists_shouldReturnUserWithEmail() {
    assertThat(userRepository.findByEmail(userFixture.getEmail()))
        .isPresent()
        .hasValueSatisfying(user -> assertThat(user).isEqualTo(userFixture));
  }

  @Test
  void findWithCardsByEmail_whenEmailExists_shouldReturnUserWithEmail() {
    assertThat(userRepository.findByEmail(userFixture.getEmail()))
        .isPresent()
        .hasValueSatisfying(user -> {
          assertThat(Hibernate.isInitialized(user.getCards()));
          assertThat(user).isEqualTo(userFixture);
          assertThat(user.getCards())
              .containsExactly(cardFixture);
        });
  }

  @Test
  void findAllByIdIn_whenIdsExist_shouldReturnMatchingUsers() {
    assertThat(userRepository.findAllByIdIn(List.of(userFixture.getId())))
        .hasSize(1)
        .containsExactly(userFixture);
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
    var mixedIds = List.of(userFixture.getId(), 999L);

    assertThat(userRepository.findAllByIdIn(mixedIds))
        .hasSize(1)
        .containsExactly(userFixture);

  }
}
