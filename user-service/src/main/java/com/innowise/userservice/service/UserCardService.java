package com.innowise.userservice.service;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.CardAlreadyExistsException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.exception.UserNotOwnCardException;
import com.innowise.userservice.model.dto.card.CardCreateRequestDto;
import com.innowise.userservice.model.dto.card.CardResponseDto;
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
  public CardResponseDto createCard(Long userId, CardCreateRequestDto dto) {
    if (cardRepository.existsByNumber(dto.number())) {
      throw new CardAlreadyExistsException(dto.number());
    }
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
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
      throw new UserNotFoundException(userId);
    }
    var card = cardRepository
        .findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));

    if (!card.getUser().getId().equals(userId)) {
      throw new UserNotOwnCardException(userId, cardId);
    }

    cardRepository.delete(card);
    cacheHelper.removeCardFromCache(userId, cardId);
  }

  public List<CardResponseDto> findUserCards(Long userId) {
    if (cacheHelper.isUserCardsCached(userId)) {
      return cacheHelper.getCardsFromCache(userId);
    }

    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }

    return cardRepository.findUserCards(userId).stream()
        .map(cardMapper::toDto)
        .toList();

  }


}
