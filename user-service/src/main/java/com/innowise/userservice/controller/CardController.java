package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.card.CardResponseDto;
import com.innowise.userservice.model.dto.card.CardUpdateRequestDto;
import com.innowise.userservice.service.CardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class CardController {

  private final CardService cardService;

  @PutMapping("/{id}")
  public ResponseEntity<CardResponseDto> update(
      @PathVariable Long id,
      @RequestBody @Validated CardUpdateRequestDto dto
  ) {
    return ResponseEntity.ok(cardService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    cardService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CardResponseDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(cardService.findById(id));
  }

  @GetMapping("/all")
  public ResponseEntity<List<CardResponseDto>> findAllByIdIn(
      @RequestParam List<Long> ids
  ) {
    return ResponseEntity.ok(cardService.findAllByIdIn(ids));
  }

}
