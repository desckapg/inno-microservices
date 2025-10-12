package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.token.TokenDto;

/**
 * Provides operations for issuing, refreshing, and validating tokens.
 */
public interface TokenService {

  /**
   * Generates a new access token using the provided refresh token.
   *
   * @param refreshToken refresh token
   * @return token payload with a new access token
   */
  TokenDto refreshAccessToken(String refreshToken);

  /**
   * Creates a new pair of access and refresh tokens for the given credentials.
   *
   * @param credentialDto credentials payload
   * @return token payload containing access and refresh tokens
   */
  TokenDto createTokens(CredentialDto credentialDto);

  /**
   * Validates the provided access token. Should fail if the token is invalid or expired.
   *
   * @param accessToken access token to validate
   */
  void validateAccessToken(String accessToken);

}
