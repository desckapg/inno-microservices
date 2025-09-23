package com.innowise.userservice.service;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.CardAlreadyExistsException;
import com.innowise.userservice.model.dto.card.CardResponseDto;
import com.innowise.userservice.model.dto.card.CardUpdateRequestDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.repository.CardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

  private final CardMapper cardMapper;
  private final CardRepository cardRepository;
  private final CacheHelper cacheHelper;

  @Transactional
  public CardResponseDto update(Long id, CardUpdateRequestDto dto) {
    Card card = cardRepository.findById(id)
        .orElseThrow(() -> new CardNotFoundException(id));

    if (!card.getNumber().equals(dto.number()) && cardRepository.existsByNumber(dto.number())) {
      throw new CardAlreadyExistsException(dto.number());
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
        .orElseThrow(() -> new CardNotFoundException(id));
    cardRepository.delete(card);
    cacheHelper.removeCardFromCache(card.getUser().getId(), id);
  }

  public CardResponseDto findById(Long id) {
    return cardRepository.findById(id)
        .map(cardMapper::toDto)
        .orElseThrow(() -> new CardNotFoundException(id));
  }

  public List<CardResponseDto> findAllByIdIn(List<Long> ids) {
    return cardRepository.findAllByIdIn(ids).stream()
        .map(cardMapper::toDto)
        .toList();
  }


}
