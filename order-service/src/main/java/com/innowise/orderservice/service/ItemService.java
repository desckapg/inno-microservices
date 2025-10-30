package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.item.ItemDto;
import java.util.List;

/**
 * Service contract for managing orderItems in the order domain.
 * Provides CRUD operations and batch retrieval via DTOs.
 */
public interface ItemService {

  /**
   * Retrieves an item by its unique identifier.
   *
   * @param id the item identifier
   * @return the item DTO if found
   */
  ItemDto findById(Long id);

  /**
   * Retrieves all orderItems.
   *
   * @return list of all item DTOs
   */
  List<ItemDto> findAll();

  /**
   * Retrieves orderItems by a list of identifiers.
   *
   * @param ids list of item identifiers
   * @return list of matching item DTOs (may be empty)
   */
  List<ItemDto> findByIdIn(List<Long> ids);

  /**
   * Creates a new item.
   *
   * @param itemDto the item data to create
   * @return the created item DTO with generated fields (e.g., id)
   */
  ItemDto create(ItemDto itemDto);

  /**
   * Updates an existing item by its identifier.
   *
   * @param id the identifier of the item to update
   * @param itemDto the new item data
   * @return the updated item DTO
   */
  ItemDto update(Long id, ItemDto itemDto);

  /**
   * Deletes an item by its identifier.
   *
   * @param id the identifier of the item to delete
   */
  void delete(Long id);

}
