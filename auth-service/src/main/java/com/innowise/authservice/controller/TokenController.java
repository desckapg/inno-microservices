package com.innowise.authservice.controller;

import com.innowise.authservice.exception.TokenException;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.token.TokenDto;
import com.innowise.authservice.service.TokenService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth/tokens")
@RequiredArgsConstructor
@RestController
@Slf4j
@NullMarked
public class TokenController {

  private final TokenService tokenService;

  @PostMapping
  public ResponseEntity<TokenDto> create(@RequestBody @Validated CredentialDto credentialDto) {
    return ResponseEntity.ok(tokenService.createTokens(credentialDto));
  }

  @PutMapping
  public ResponseEntity<TokenDto> refresh(
      @RequestHeader(name = "X-Refresh-Token")
      @NotBlank(message = "Refresh token required") String refreshToken) {
    return ResponseEntity.ok(tokenService.refreshAccessToken(refreshToken));
  }

  @RequestMapping(method = RequestMethod.HEAD)
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
