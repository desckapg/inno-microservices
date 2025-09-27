package com.innowise.userservice.service.impl;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.ResourceAlreadyExistsException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.CardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

  private final CardMapper cardMapper;
  private final CardRepository cardRepository;
  private final UserRepository userRepository;
  private final CacheHelper cacheHelper;

  @Transactional
  public CardDto create(CardDto dto) {
    if (cardRepository.existsByNumber(dto.number())) {
      throw ResourceAlreadyExistsException.byField("Card", "number", dto.number());
    }
    if (!userRepository.existsById(dto.userId())) {
      throw ResourceNotFoundException.byField("User", "id", dto.userId());
    }
    var card = cardMapper.toEntity(dto);
    card.setUser(userRepository.getReferenceById(dto.userId()));

    var savedCard = cardRepository.save(card);
    var cardResponseDto = cardMapper.toDto(savedCard);

    cacheHelper.addCardToCache(dto.userId(), cardResponseDto);

    return cardResponseDto;
  }

  @Transactional
  public CardDto update(Long id, CardDto dto) {
    Card card = cardRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.byId("Card", id));

    if (!card.getNumber().equals(dto.number()) && cardRepository.existsByNumber(dto.number())) {
      throw ResourceAlreadyExistsException.byField("Card", "number", dto.number());
    }

    card.setHolder(dto.holder());
    card.setExpirationDate(dto.expirationDate());
    card.setNumber(dto.number());

    var savedCard = cardRepository.save(card);
    cacheHelper.updateCardInCache(savedCard.getUser().getId(), cardMapper.toDto(savedCard));

    return cardMapper.toDto(savedCard);
  }

  @Transactional
  public void delete(Long id) {
    var card = cardRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.byId("Card", id));
    cardRepository.delete(card);
    cacheHelper.removeCardFromCache(card.getUser().getId(), id);
  }

  public CardDto findById(Long id) {
    return cardRepository.findById(id)
        .map(cardMapper::toDto)
        .orElseThrow(() -> ResourceNotFoundException.byId("Card", id));
  }

  public List<CardDto> findAllByIdIn(List<Long> ids) {
    return cardRepository.findAllByIdIn(ids).stream()
        .map(cardMapper::toDto)
        .toList();
  }


  public List<CardDto> findAll() {
    return cardRepository.findAll().stream()
        .map(cardMapper::toDto)
        .toList();
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
