package com.innowise.userservice.service;

import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.CardWithNumberExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.model.dto.card.CardCreateRequestDto;
import com.innowise.userservice.model.dto.card.CardResponseDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCardService {

  private final UserRepository userRepository;
  private final CardRepository cardRepository;
  private final EntityManager entityManager;
  private final CardMapper cardMapper;

  @Transactional
  public CardResponseDto createCard(Long userId, CardCreateRequestDto dto) {
    if (cardRepository.existsByNumber(dto.number())) {
      throw new CardWithNumberExistsException(dto.number());
    }
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    var card = cardMapper.toEntity(dto);
    card.setUser(entityManager.getReference(User.class, userId));
    return cardMapper.toDto(cardRepository.save(card));
  }

  @Transactional
  public void deleteCard(Long userId, Long cardId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    var card = cardRepository
        .findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));

    if (!card.getUser().getId().equals(userId)) {
      throw new UserNotFoundException(userId);
    }
    cardRepository.delete(card);
  }


  public List<CardResponseDto> findUserCards(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    return cardRepository.findUserCards(userId).stream().map(cardMapper::toDto).toList();
  }


}
