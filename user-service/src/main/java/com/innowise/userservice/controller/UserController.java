package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.validation.group.OnCreate;
import com.innowise.userservice.validation.group.OnUpdate;
import jakarta.validation.constraints.Email;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing users and accessing their cards.
 * Base path: /api/v1/users
 */
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Validated
@NullMarked
public class UserController {

  private final UserService userService;
  private final CardService cardService;

  /**
   * Searches users by optional filters:
   * - email: exact match
   * - ids: batch retrieval
   * If no filters provided returns all users. Supplying both email and ids results in 400.
   * @param email optional email
   * @param ids optional list of user ids
   * @return list of users
   * @throws org.springframework.web.server.ResponseStatusException if both filters are used
   */
  @GetMapping
  public ResponseEntity<List<UserDto>> find(
      @Nullable @RequestParam(name = "email", required = false) @Email String email,
      @Nullable @RequestParam(name = "ids", required = false) List<Long> ids) {

    boolean hasEmail = email != null && !email.isBlank();
    boolean hasIds = ids != null && !ids.isEmpty();

    if (hasEmail && hasIds) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot filter by both email and ids simultaneously"
      );
    }
    if (hasEmail) {
      return ResponseEntity.ok(List.of(userService.findByEmail(email)));
    }
    if (hasIds) {
      return ResponseEntity.ok(userService.findAllByIdIn(ids));
    }
    return ResponseEntity.ok(userService.findAll());
  }

  /**
   * Retrieves a user by id.
   * @param id user identifier
   * @return user data
   */
  @GetMapping("/{id}")
  public ResponseEntity<UserDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.findById(id));
  }

  /**
   * Creates a new user.
   * @param dto user payload (create validation group)
   * @return created user
   */
  @PostMapping
  public ResponseEntity<UserDto> create(@RequestBody @Validated(OnCreate.class) UserDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
  }

  /**
   * Updates an existing user.
   * @param id user identifier
   * @param dto user payload (update validation group)
   * @return updated user
   */
  @PutMapping("/{id}")
  public ResponseEntity<UserDto> update(
      @PathVariable Long id,
      @RequestBody @Validated(OnUpdate.class) UserDto dto) {
    return ResponseEntity.ok(userService.update(id, dto));
  }

  /**
   * Deletes a user by id.
   * @param id user identifier
   * @return 204 No Content on success
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns all cards belonging to the specified user.
   * @param userId user identifier
   * @return list of user's cards
   */
  @GetMapping("/{userId}/cards")
  public ResponseEntity<List<CardDto>> findUserCards(@PathVariable Long userId) {
    return ResponseEntity.ok(cardService.findUserCards(userId));
  }

}
