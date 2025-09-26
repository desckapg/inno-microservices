package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.service.CardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/cards/")
@Validated
@RequiredArgsConstructor
@NullMarked
public class CardController {

  private final CardService cardService;

  @PutMapping("/{id}")
  public ResponseEntity<CardDto> update(
      @PathVariable Long id,
      @RequestBody @Validated CardDto dto
  ) {
    return ResponseEntity.ok(cardService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    cardService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CardDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(cardService.findById(id));
  }

  @GetMapping("/all")
  public ResponseEntity<List<CardDto>> findAllByIdIn(
      @RequestParam List<Long> ids
  ) {
    return ResponseEntity.ok(cardService.findAllByIdIn(ids));
  }

}
