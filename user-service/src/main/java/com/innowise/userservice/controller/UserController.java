package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.card.CardCreateRequestDto;
import com.innowise.userservice.model.dto.card.CardResponseDto;
import com.innowise.userservice.model.dto.user.UserCreateRequestDto;
import com.innowise.userservice.model.dto.user.UserResponseDto;
import com.innowise.userservice.model.dto.user.UserUpdateRequestDto;
import com.innowise.userservice.service.UserCardService;
import com.innowise.userservice.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("api/v1/users/")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;
  private final UserCardService userCardService;

  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> findById(
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean includeCards
  ) {
    return ResponseEntity.ok(userService.findById(id, includeCards));
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<UserResponseDto> findByEmail(
      @PathVariable @NotNull @Email String email,
      @RequestParam(defaultValue = "false") boolean includeCards
  ) {
    return ResponseEntity.ok(userService.findByEmail(email, includeCards));
  }

  @PostMapping("/create")
  public ResponseEntity<UserResponseDto> create(@RequestBody @Validated UserCreateRequestDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDto> update(
      @PathVariable Long id,
      @RequestBody @Validated UserUpdateRequestDto dto) {
    return ResponseEntity.ok(userService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }


  @PostMapping("/{userId}/cards")
  public ResponseEntity<CardResponseDto> createCard(
      @PathVariable Long userId,
      @RequestBody @Validated CardCreateRequestDto dto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userCardService.createCard(userId, dto));
  }

  @DeleteMapping("/{userId}/cards/{cardId}")
  public ResponseEntity<Void> deleteCard(
      @PathVariable Long userId,
      @PathVariable Long cardId
  ) {
    userCardService.deleteCard(userId, cardId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{userId}/cards")
  public ResponseEntity<List<CardResponseDto>> findUserCards(@PathVariable Long userId) {
    return ResponseEntity.ok(userCardService.findUserCards(userId));
  }

}
