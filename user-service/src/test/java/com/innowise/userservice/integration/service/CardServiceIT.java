package com.innowise.userservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.ResourceAlreadyExistsException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.ServiceIT;
import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.service.impl.CardServiceImpl;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@ServiceIT
@RequiredArgsConstructor
class CardServiceIT extends AbstractIntegrationTest {

  private User userFixture;
  private Card cardFixture;

  private final CardServiceImpl cardService;
  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;
  private final CardMapper cardMapper;
  private final CacheHelper cacheHelper;

  @BeforeAll
  void prepareFixtures() {
    userFixture = Users.buildWithoutId();
    cardFixture = Cards.buildWithoutId(userFixture);
    transactionTemplate.executeWithoutResult(status ->
        entityManager.persist(userFixture));
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(status ->
        entityManager.remove(entityManager.find(User.class, userFixture.getId())));
  }

  @AfterEach
  void cleanupCache() {
    cacheHelper.invalidate();
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
        .isInstanceOf(ResourceNotFoundException.class);
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
    var updateDto = CardDto.builder()
        .number(cardFixture.getNumber())
        .holder(cardFixture.getHolder())
        .expirationDate(cardFixture.getExpirationDate())
        .build();

    assertThatThrownBy(() -> cardService.update(Long.MAX_VALUE, updateDto))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @Transactional
  void update_whenCardExists_shouldReturnUpdatedCardResponseDto() {
    var updateDto = CardDto.builder()
        .number(cardFixture.getNumber())
        .holder("Updated Holder")
        .expirationDate(cardFixture.getExpirationDate())
        .build();

    var updatedCard = cardService.update(cardFixture.getId(), updateDto);

    assertThat(updatedCard.number()).isEqualTo(updateDto.number());
    assertThat(updatedCard.holder()).isEqualTo(updateDto.holder());
    assertThat(updatedCard.expirationDate()).isEqualTo(updateDto.expirationDate());
  }

  @Test
  @Transactional
  void delete_whenCardNotExists_shouldThrowCardNotFoundException() {
    assertThatThrownBy(() -> cardService.delete(Long.MAX_VALUE))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Card");
  }

  @Test
  @Transactional
  void delete_whenCardExists_shouldSuccessfullyDeleteCard() {
    assertThatNoException().isThrownBy(() -> cardService.delete(cardFixture.getId()));
    assertThat(entityManager.find(Card.class, cardFixture.getId())).isNull();
  }

  @Test
  void findUserCards_whenUserExists_shouldReturnListOfCardResponseDto() {
    assertThat(cardService.findUserCards(userFixture.getId()))
        .containsExactlyElementsOf(
            userFixture.getCards().stream()
                .map(cardMapper::toDto)
                .toList()
        );
  }

  @Test
  void findUserCards_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> cardService.findUserCards(Long.MAX_VALUE))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User", "id");
  }

  @Test
  @Transactional
  void createCard_whenUserExistsAndCardNumberDoesNotExist_shouldReturnResponseDto() {
    var newCardDto = Cards.build(userFixture);
    var createdCardDto = cardService.create(CardDto.builder()
        .number(newCardDto.getNumber())
        .holder(newCardDto.getHolder())
        .expirationDate(newCardDto.getExpirationDate())
        .userId(userFixture.getId())
        .build());

    assertThat(createdCardDto.id()).isNotNull();
    assertThat(createdCardDto.userId()).isEqualTo(userFixture.getId());
    assertThat(createdCardDto)
        .usingRecursiveComparison()
        .ignoringFields("id", "userId")
        .isEqualTo(newCardDto);
  }

  @Test
  @Transactional
  void createCard_whenCardWithNumberExists_shouldAlreadyExistsException() {
    var newCard = Cards.build();

    var createDto = CardDto.builder()
        .number(cardFixture.getNumber())
        .holder(newCard.getHolder())
        .expirationDate(newCard.getExpirationDate())
        .userId(Long.MAX_VALUE)
        .build();

    assertThatThrownBy(() -> cardService.create(createDto))
        .isInstanceOf(ResourceAlreadyExistsException.class)
        .hasMessageContaining("Card", "number");
  }

  @Test
  @Transactional
  void create_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
    var newCardDto = Cards.build();

    var createDto = CardDto.builder()
        .number(newCardDto.getNumber())
        .holder(newCardDto.getHolder())
        .expirationDate(newCardDto.getExpirationDate())
        .userId(Long.MAX_VALUE)
        .build();

    assertThatThrownBy(() -> cardService.create(createDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User");
  }

}
