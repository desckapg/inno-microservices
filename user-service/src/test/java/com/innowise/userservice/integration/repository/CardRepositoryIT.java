package com.innowise.userservice.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.IT;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.support.TransactionTemplate;

@IT
@RequiredArgsConstructor
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class CardRepositoryIT extends AbstractIntegrationTest {

  private User userFixture;
  private Card cardFixture;

  private final TestEntityManager entityManager;
  private final CardRepository cardRepository;
  private final TransactionTemplate transactionTemplate;

  @BeforeAll
  void prepareFixtures() {
    userFixture = Users.buildWithoutId();
    cardFixture = Cards.buildWithoutId(userFixture);
    userFixture.addCard(cardFixture);
    transactionTemplate.executeWithoutResult(status -> {
      entityManager.persist(userFixture);
    });
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(_ -> {
      var user = entityManager.find(User.class, userFixture.getId());
      if (user != null) {
        entityManager.remove(user);
      }
    });
  }

  @AfterEach
  void clearPersistenceContext() {
    transactionTemplate.executeWithoutResult(_ -> entityManager.clear());
  }

  @Test
  void findAllByIdIn_whenIdsNotExist_shouldReturnEmptyList() {
    var nonExistentIds = List.of(Long.MAX_VALUE, Long.MIN_VALUE);

    assertThat(cardRepository.findAllByIdIn(nonExistentIds))
        .isEmpty();
  }

  @Test
  void findAllByIdIn_whenEmptyIdsList_shouldReturnEmptyList() {
    assertThat(cardRepository.findAllByIdIn(List.of()))
        .isEmpty();
  }

  @Test
  void findAllByIdIn_whenMixedIds_shouldReturnOnlyExistingUsers() {
    var mixedIds = List.of(cardFixture.getId(), 999L);

    assertThat(cardRepository.findAllByIdIn(mixedIds))
        .containsExactlyInAnyOrder(cardFixture);

  }

}
