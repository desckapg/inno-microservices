package com.innowise.userservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.innowise.userservice.exception.CardAlreadyExistsException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.exception.UserNotOwnCardException;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.ServiceIT;
import com.innowise.userservice.model.dto.card.CardCreateRequestDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.model.mapper.UserMapper;
import com.innowise.userservice.service.UserCardService;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@ServiceIT
@RequiredArgsConstructor
class UserCardServiceIT extends AbstractIntegrationTest {

  private User userFixture;
  private Card cardFixture;

  private final UserCardService userCardService;

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  private final UserMapper userMapper;
  private final CardMapper cardMapper;

  @BeforeAll
  void prepareFixtures() {
    userFixture = Users.buildWithoutId();
    cardFixture = Cards.buildWithoutId(userFixture);
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
  void findUserCards_whenUserExists_shouldReturnListOfCardResponseDto() {
    assertThat(userCardService.findUserCards(userFixture.getId()))
        .containsExactlyElementsOf(
            userFixture.getCards().stream()
                .map(cardMapper::toDto)
                .toList()
        );
  }

  @Test
  void findUserCards_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userCardService.findUserCards(Long.MAX_VALUE))
        .isInstanceOf(com.innowise.userservice.exception.UserNotFoundException.class);
  }

  @Test
  @Transactional
  void createCard_whenUserExistsAndCardNumberDoesNotExist_shouldReturnCardResponseDto() {
    var newCardDto = Cards.build(userFixture);
    var createdCardDto = userCardService.createCard(userFixture.getId(), new CardCreateRequestDto(
        newCardDto.getNumber(),
        newCardDto.getHolder(),
        newCardDto.getExpirationDate()
    ));
    assertThat(createdCardDto.id()).isNotNull();
    assertThat(createdCardDto.userId()).isEqualTo(userFixture.getId());
    assertThat(createdCardDto)
        .usingRecursiveComparison()
        .ignoringFields("id", "userId")
        .isEqualTo(newCardDto);
  }

  @Test
  @Transactional
  void createCard_whenCardWithNumberExists_shouldCardAlreadyExistsException() {
    var newCard = Cards.build();
    assertThatThrownBy(() -> userCardService.createCard(Long.MAX_VALUE, new CardCreateRequestDto(
        cardFixture.getNumber(),
        newCard.getHolder(),
        newCard.getExpirationDate()
    ))).isInstanceOf(CardAlreadyExistsException.class);
  }

  @Test
  @Transactional
  void createCard_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
    var newCardDto = Cards.build();
    assertThatThrownBy(() -> userCardService.createCard(Long.MAX_VALUE, new CardCreateRequestDto(
        newCardDto.getNumber(),
        newCardDto.getHolder(),
        newCardDto.getExpirationDate()
    ))).isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @Transactional
  void deleteCard_whenUserAndCardExistAndCardBelongsToUser_shouldDeleteCard() {
    userCardService.deleteCard(userFixture.getId(), cardFixture.getId());
    assertThat(entityManager.find(Card.class, cardFixture.getId())).isNull();
  }

  @Test
  @Transactional
  void deleteCard_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userCardService.deleteCard(Long.MAX_VALUE, cardFixture.getId()))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @Transactional
  void deleteCard_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
    assertThatThrownBy(() -> userCardService.deleteCard(userFixture.getId(), Long.MAX_VALUE))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  @Transactional
  void deleteCard_whenUserNotOwnCard_shouldThrowUserNotOwnCardException() {
    var newUser = Users.buildWithoutId();
    entityManager.persist(newUser);

    assertThatThrownBy(() -> userCardService.deleteCard(newUser.getId(), cardFixture.getId()))
        .isInstanceOf(UserNotOwnCardException.class);
  }



}
