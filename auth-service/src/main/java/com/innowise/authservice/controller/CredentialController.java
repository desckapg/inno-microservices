package com.innowise.authservice.controller;

import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.user.UserDto;
import com.innowise.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@NullMarked
@RequestMapping("/api/v1/auth/credentials")
public class CredentialController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> save(@RequestBody @Validated CredentialDto credentialDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.saveCredentials(credentialDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.deleteCredentials(id);
    return ResponseEntity.noContent().build();
  }


}
