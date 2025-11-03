package com.innowise.authservice.controller;

import com.innowise.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@NullMarked
@RequestMapping("/api/v1/auth/users")
public class UserController {

  private final UserService userService;

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.innowise.auth.model.Role).MANAGER.getAuthority())")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }


}
