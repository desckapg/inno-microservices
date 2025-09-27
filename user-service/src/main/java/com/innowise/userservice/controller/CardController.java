package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.validation.group.OnCreate;
import com.innowise.userservice.validation.group.OnUpdate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing CRUD operations for payment cards.
 * Base path: /api/v1/cards
 */
@RestController
@RequestMapping("api/v1/cards")
@Validated
@RequiredArgsConstructor
@NullMarked
public class CardController {

  private final CardService cardService;

  /**
   * Returns all cards or a subset by ids.
   * @param ids optional list of card identifiers to filter (if empty or null returns all)
   * @return list of cards
   */
  @GetMapping("/")
  public ResponseEntity<List<CardDto>> findAll(@RequestParam(name = "ids", required = false) List<Long> ids) {
    if (ids.isEmpty()) {
      return ResponseEntity.ok(cardService.findAll());
    } else {
      return ResponseEntity.ok(cardService.findAllByIdIn(ids));
    }
  }

  /**
   * Creates a new card.
   * @param dto card payload
   * @return created card with generated id
   */
  @PostMapping("/")
  public ResponseEntity<CardDto> create(@RequestBody @Validated({OnCreate.class}) CardDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(cardService.create(dto));
  }

  /**
   * Updates an existing card by id.
   * @param id card identifier
   * @param dto updated card data
   * @return updated card
   */
  @PutMapping("/{id}")
  public ResponseEntity<CardDto> update(
      @PathVariable Long id,
      @RequestBody @Validated({OnUpdate.class}) CardDto dto
  ) {
    return ResponseEntity.ok(cardService.update(id, dto));
  }

  /**
   * Deletes a card by id.
   * @param id card identifier
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    cardService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Retrieves a single card by id.
   * @param id card identifier
   * @return card data
   */
  @GetMapping("/{id}")
  public ResponseEntity<CardDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(cardService.findById(id));
  }

}
