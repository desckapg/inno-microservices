package com.innowise.authservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.authservice.exception.TokenException;
import com.innowise.authservice.model.dto.CredentialsConstraints;
import com.innowise.authservice.model.dto.UserConstraints;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.token.TokenDto;
import com.innowise.authservice.model.dto.user.UserAuthInfoDto;
import com.innowise.authservice.service.TokenService;
import com.innowise.authservice.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
@Slf4j
@NullMarked
public class AuthController {

  private final TokenService tokenService;
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<UserAuthInfoDto> register(
      @RequestBody
      @Validated(UserConstraints.Register.class)
      UserAuthInfoDto userAuthInfoDto
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.register(userAuthInfoDto));
  }

  @PostMapping("/login")
  public ResponseEntity<TokenDto> login(@RequestBody @Validated(CredentialsConstraints.Base.class) CredentialDto credentialDto) {
    return ResponseEntity.ok(tokenService.createTokens(credentialDto));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenDto> refresh(
      @RequestHeader(name = "X-Refresh-Token")
      @NotBlank(message = "Refresh token required") String refreshToken) {
    return ResponseEntity.ok(tokenService.refreshAccessToken(refreshToken));
  }

  @GetMapping("/validate")
  public ResponseEntity<Void> validate(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    String accessToken = extractBearer(authorization);
    tokenService.validateAccessToken(accessToken);
    return ResponseEntity.ok().build();
  }

  private String extractBearer(String header) {
    if (!header.startsWith("Bearer ")) {
      throw TokenException.missing();
    }
    return header.substring(7);
  }

}
