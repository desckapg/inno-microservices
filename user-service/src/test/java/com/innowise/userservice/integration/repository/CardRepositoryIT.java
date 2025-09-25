package com.innowise.userservice.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.JpaRepositoryIT;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.CardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.support.TransactionTemplate;

@JpaRepositoryIT
@RequiredArgsConstructor
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class CardRepositoryIT extends AbstractIntegrationTest {

  private Long userFixtureId;
  private Long cardFixtureId;

  private final TestEntityManager entityManager;
  private final CardRepository cardRepository;
  private final TransactionTemplate transactionTemplate;

  @BeforeAll
  void prepareFixtures() {
    var userFixture = userWithoutCardsBuilder.build();
    var cardFixture = cardWithoutUserBuilder.build();
    userFixture.addCard(cardFixture);
    transactionTemplate.executeWithoutResult(status ->
        entityManager.persistAndFlush(userFixture));
    userFixtureId = userFixture.getId();
    cardFixtureId = cardFixture.getId();
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(status -> {
      var card = entityManager.find(Card.class, cardFixtureId);
      var user = entityManager.find(User.class, userFixtureId);
      if (card != null) {
        entityManager.remove(card);
      }
      if (user != null) {
        entityManager.remove(user);
      }
    });
  }

  @AfterEach
  void clearPersistenceContext() {
    entityManager.clear();
  }

    @Test
  void findAllByIdIn_whenIdsNotExist_shouldReturnEmptyList() {
    var nonExistentIds = List.of(999L, 1000L);

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
    var mixedIds = List.of(cardFixtureId, 999L);

    assertThat(cardRepository.findAllByIdIn(mixedIds))
        .hasSize(1)
        .extracting(Card::getId)
        .containsExactlyInAnyOrder(cardFixtureId);

  }

}
