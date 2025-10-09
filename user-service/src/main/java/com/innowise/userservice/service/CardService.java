package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.card.CardDto;
import java.util.List;

/**
 * Service responsible for managing user payment cards.
 * Provides CRUD operations and lookup methods including retrieval by owner.
 */
public interface CardService {

  /**
   * Creates a new card.
   * @param dto card data
   * @return persisted card
   */
  CardDto create(CardDto dto) ;

  /**
   * Updates an existing card.
   * @param id card identifier
   * @param dto new data
   * @return updated card
   */
  CardDto update(Long id, CardDto dto);

  /**
   * Deletes a card by id.
   * @param id card identifier
   */
  void delete(Long id);

  /**
   * Finds a card by id.
   * @param id card identifier
   * @return card DTO
   */
  CardDto findById(Long id);

  /**
   * Retrieves cards by a list of identifiers.
   * @param ids list of card ids
   * @return list of cards
   */
  List<CardDto> findAllByIdIn(List<Long> ids) ;

  /**
   * Returns all cards.
   * @return list of cards
   */
  List<CardDto> findAll();

  /**
   * Returns cards owned by the specified user.
   * @param userId user identifier
   * @return list of user's cards
   */
  List<CardDto> findUserCards(Long userId);

}
