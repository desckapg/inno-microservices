package com.innowise.userservice.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.CardAlreadyExistsException;
import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.dto.card.CardUpdateRequestDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class CardServiceTest {

  @Mock
  private CardRepository cardRepository;

  @Mock
  private CacheHelper cacheHelper;

  private CardMapper cardMapper = Mappers.getMapper(CardMapper.class);

  @InjectMocks
  private CardService cardService;

  @BeforeEach
  void prepare() {
    cardService = new CardService(
        cardMapper,
        cardRepository,
        cacheHelper
    );
  }

  @Test
  void update_whenValidData_shouldUpdateCard() {
    var user = Users.build();
    Card existingCard = Cards.build(user);


    Card updatedCard = Cards.build(existingCard.getId());
    updatedCard.setUser(user);

    CardUpdateRequestDto updateDto = new CardUpdateRequestDto(
        updatedCard.getNumber(),
        updatedCard.getHolder(),
        updatedCard.getExpirationDate()
    );

    updatedCard.setExpirationDate(LocalDate.now().plusYears(2L));

    when(cardRepository.findById(existingCard.getId()))
        .thenReturn(Optional.of(existingCard));
    when(cardRepository.existsByNumber(updatedCard.getNumber()))
        .thenReturn(false);
    when(cardRepository.save(any(Card.class)))
        .thenReturn(updatedCard);

    CardDto result = cardService.update(existingCard.getId(), updateDto);

    assertThat(result).isEqualTo(cardMapper.toDto(updatedCard));
    verify(cardRepository).save(any(Card.class));
  }

  @Test
  void update_whenCardNotFound_shouldThrowCardNotFoundException() {
    var nonExistingCard = Cards.build();
    CardUpdateRequestDto updateDto = new CardUpdateRequestDto(
        nonExistingCard.getNumber(),
        nonExistingCard.getHolder(),
        nonExistingCard.getExpirationDate()
    );

    when(cardRepository.findById(nonExistingCard.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> cardService.update(nonExistingCard.getId(), updateDto))
        .isInstanceOf(CardNotFoundException.class);

    verify(cardRepository, never()).save(any());
  }

  @Test
  void update_whenNumberExistsForAnotherCard_shouldThrowCardAlreadyExists() {
    Card existingCard = Cards.build(Users.build());
    Card updatingCard = Cards.build(existingCard.getId());

    CardUpdateRequestDto updateDto = new CardUpdateRequestDto(
        updatingCard.getNumber(),
        updatingCard.getHolder(),
        updatingCard.getExpirationDate()
    );

    when(cardRepository.findById(existingCard.getId()))
        .thenReturn(Optional.of(existingCard));
    when(cardRepository.existsByNumber(updatingCard.getNumber()))
        .thenReturn(true);

    assertThatThrownBy(() -> cardService.update(existingCard.getId(), updateDto))
        .isInstanceOf(CardAlreadyExistsException.class);

    verify(cardRepository, never()).save(any());
  }

  @Test
  void delete_whenCardExists_shouldDeleteCard() {
    Card card = Cards.build(Users.build());

    when(cardRepository.findById(card.getId()))
        .thenReturn(Optional.of(card));

    cardService.delete(card.getId());

    verify(cardRepository, times(1)).delete(card);
  }

  @Test
  void delete_whenCardNotFound_shouldThrowCardNotFoundException() {
    Long cardId = 1L;

    when(cardRepository.findById(cardId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> cardService.delete(cardId))
        .isInstanceOf(CardNotFoundException.class);

    verify(cardRepository, never()).delete(any());
  }

  @Test
  void findById_whenCardExists_shouldReturnCard() {
    var user = Users.build();
    Card card = Cards.build(user);

    when(cardRepository.findById(card.getId()))
        .thenReturn(Optional.of(card));

    CardDto result = cardService.findById(card.getId());

    assertThat(result)
        .isEqualTo(cardMapper.toDto(card));
    verify(cardRepository, times(1)).findById(card.getId());
  }

  @Test
  void findById_whenCardNotFound_shouldThrowCardNotFoundException() {
    Long cardId = 1L;

    when(cardRepository.findById(cardId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> cardService.findById(cardId))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  void findAllById_In_whenCardsExist_shouldReturnCardsList() {
    var user1 = Users.build();
    var user2 = Users.build();
    List<Long> ids = List.of(user1.getId(), user2.getId());
    List<Card> cards = List.of(Cards.build(user1), Cards.build(user2));

    when(cardRepository.findAllByIdIn(ids))
        .thenReturn(cards);

    List<CardDto> result = cardService.findAllByIdIn(ids);

    assertThat(result)
        .containsExactlyElementsOf(
            cards.stream()
                .map(cardMapper::toDto)
                .toList()
        );
  }

  @Test
  void findAllById_In_whenNoCards_shouldReturnEmptyList() {
    List<Long> ids = List.of(1L, 2L);

    when(cardRepository.findAllByIdIn(ids)).thenReturn(List.of());

    List<CardDto> result = cardService.findAllByIdIn(ids);

    assertThat(result).isEmpty();
  }
}

