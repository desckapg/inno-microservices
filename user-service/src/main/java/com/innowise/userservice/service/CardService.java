package com.innowise.userservice.service;

import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.CardWithNumberExistsException;
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

  @Transactional
  public CardResponseDto update(Long id, CardUpdateRequestDto dto) {
    Card card = cardRepository.findById(id)
        .orElseThrow(() -> new CardNotFoundException(id));
    card.setHolder(dto.holder());
    card.setExpirationDate(dto.expirationDate());
    if (!card.getNumber().equals(dto.number()) && cardRepository.existsByNumber(dto.number())) {
      throw new CardWithNumberExistsException(dto.number());
    }
    card.setNumber(dto.number());
    return cardMapper.toDto(cardRepository.save(card));
  }

  @Transactional
  public void delete(Long id) {
    cardRepository.delete(
        cardRepository.findById(id)
            .orElseThrow(() -> new CardNotFoundException(id))
    );
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
