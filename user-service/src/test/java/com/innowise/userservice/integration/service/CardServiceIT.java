package com.innowise.userservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.ServiceIT;
import com.innowise.userservice.model.dto.card.CardUpdateRequestDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@ServiceIT
@RequiredArgsConstructor
public class CardServiceIT extends AbstractIntegrationTest {

  private User userFixture;
  private Card cardFixture;

  private final CardService cardService;

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  private final CardMapper cardMapper;

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
    transactionTemplate.executeWithoutResult(status -> {
      entityManager.remove(entityManager.find(User.class, userFixture.getId()));
    });
  }

  @Test
  void contextLoads() {
    assertThat(cardService).isNotNull();
  }

  @Test
  void findById_whenCardExists_shouldReturnCardResponseDto() {
    assertThat(cardService.findById(cardFixture.getId()))
        .isEqualTo(cardMapper.toDto(cardFixture));
  }

  @Test
  void findById_whenCardNotExists_shouldThrowCardNotFoundException() {
    assertThatThrownBy(() -> cardService.findById(Long.MAX_VALUE))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  void findAllByIdIn_whenCardsExist_shouldReturnListOfCardResponseDto() {
    assertThat(cardService.findAllByIdIn(
        List.of(cardFixture.getId(), Long.MAX_VALUE)))
        .containsExactly(cardMapper.toDto(cardFixture));
  }

  @Test
  void findAllByIdIn_whenCardsNotExist_shouldReturnEmptyList() {
    assertThat(cardService.findAllByIdIn(List.of(Long.MAX_VALUE)))
        .isEmpty();
  }

  @Test
  @Transactional
  void update_whenCardNotExists_shouldThrowCardNotFoundException() {
    assertThatThrownBy(() -> cardService.update(Long.MAX_VALUE, new CardUpdateRequestDto(
        cardFixture.getNumber(),
        cardFixture.getHolder(),
        cardFixture.getExpirationDate()
    )))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  @Transactional
  void update_whenCardExists_shouldReturnUpdatedCardResponseDto() {
    var updateDto = new CardUpdateRequestDto(
        cardFixture.getNumber(),
        "Updated Holder",
        cardFixture.getExpirationDate()
    );
    var updatedCard = cardService.update(cardFixture.getId(), updateDto);
    assertThat(updatedCard.number()).isEqualTo(updateDto.number());
    assertThat(updatedCard.holder()).isEqualTo(updateDto.holder());
    assertThat(updatedCard.expirationDate()).isEqualTo(updateDto.expirationDate());
  }

  @Test
  @Transactional
  void delete_whenCardNotExists_shouldThrowCardNotFoundException() {
    assertThatThrownBy(() -> cardService.delete(Long.MAX_VALUE))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  @Transactional
  void delete_whenCardExists_shouldSuccessfullyDeleteCard() {
    assertThatNoException().isThrownBy(() -> cardService.delete(cardFixture.getId()));
    assertThat(entityManager.find(Card.class, cardFixture.getId())).isNull();
  }

}
