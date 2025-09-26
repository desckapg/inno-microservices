package com.innowise.userservice.service;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.ResourceAlreadyExistsException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.exception.UserNotOwnCardException;
import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCardService {

  private final UserRepository userRepository;
  private final CardRepository cardRepository;
  private final CardMapper cardMapper;
  private final CacheHelper cacheHelper;

  @Transactional
  public CardDto createCard(Long userId, CardDto dto) {
    if (cardRepository.existsByNumber(dto.number())) {
      throw ResourceAlreadyExistsException.byField("Card", "number", dto.number());
    }
    if (!userRepository.existsById(userId)) {
      throw ResourceNotFoundException.byField("User", "id", userId);
    }
    var card = cardMapper.toEntity(dto);
    card.setUser(userRepository.getReferenceById(userId));

    var savedCard = cardRepository.save(card);
    var cardResponseDto = cardMapper.toDto(savedCard);

    cacheHelper.addCardToCache(userId, cardResponseDto);

    return cardResponseDto;
  }

  @Transactional
  public void deleteCard(Long userId, Long cardId) {
    if (!userRepository.existsById(userId)) {
      throw ResourceNotFoundException.byId("User", userId);
    }
    var card = cardRepository
        .findById(cardId)
        .orElseThrow(() -> ResourceNotFoundException.byField("Card", "id", cardId));

    if (!card.getUser().getId().equals(userId)) {
      throw new UserNotOwnCardException(userId, cardId);
    }

    cardRepository.delete(card);
    cacheHelper.removeCardFromCache(userId, cardId);
  }

  public List<CardDto> findUserCards(Long userId) {
    if (cacheHelper.isUserCached(userId)) {
      return cacheHelper.getCardsFromCache(userId);
    }

    if (!userRepository.existsById(userId)) {
      throw ResourceNotFoundException.byId("User", userId);
    }

    return cardRepository.findUserCards(userId).stream()
        .map(cardMapper::toDto)
        .toList();

  }

}
