package com.innowise.authservice.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.innowise.authservice.exception.AuthFailedException;
import com.innowise.authservice.exception.ResourceNotFoundException;
import com.innowise.authservice.exception.TokenException;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.token.TokenDto;
import com.innowise.authservice.model.dto.user.UserDto;
import com.innowise.authservice.service.TokenService;
import com.innowise.authservice.service.UserService;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@NullMarked
public class TokenServiceImpl implements TokenService {

  private final UserService userService;

  @Value("${spring.application.name}")
  private String issuer;

  private static final String USER_SERVICE_ID_CLAIM = "userId";
  private static final String USER_ROLES_CLAIM = "roles";

  private String createAccessToken(Long userAuthId, Long userProfileId, List<String> roles) {
    return JWT.create()
        .withExpiresAt(Instant.from(ZonedDateTime.now().plusSeconds(getExpiration())))
        .withIssuer(issuer)
        .withSubject(userAuthId.toString())
        .withClaim(USER_SERVICE_ID_CLAIM, userProfileId)
        .withArrayClaim(USER_ROLES_CLAIM, roles.toArray(new String[0]))
        .withClaim("type", "access")
        .sign(Algorithm.HMAC256(getAccessSecretKey()));
  }

  private String createRefreshToken(Long userAuthId) {
    return JWT.create()
        .withExpiresAt(Instant.from(ZonedDateTime.now().plusSeconds(getRefreshExpiration())))
        .withIssuer(issuer)
        .withSubject(userAuthId.toString())
        .withClaim("type", "refresh")
        .sign(Algorithm.HMAC256(getRefreshSecretKey()));
  }

  @Override
  public TokenDto refreshAccessToken(String refreshToken) {
    try {
      var refreshVerifier = createRefreshTokenJwtVerifier();
      var decodedRefreshToken = refreshVerifier.verify(refreshToken);
      Long userAuthId = Long.parseUnsignedLong(decodedRefreshToken.getSubject());
      UserDto userDto = tryAuthenticateUser(userAuthId);
      return TokenDto.builder()
          .accessToken(createAccessToken(
              userDto.id(),
              userDto.userId(),
              userDto.roles()
          ))
          .build();
    } catch (JWTVerificationException e) {
      throw TokenException.fromJwtException(e);
    }
  }

  public TokenDto createTokens(CredentialDto credentialDto) {
    var user = userService.findByLoginAndPassword(credentialDto.login(), credentialDto.password());
    return TokenDto.builder()
        .accessToken(createAccessToken(
            user.id(),
            user.userId(),
            user.roles()
        ))
        .refreshToken(createRefreshToken(user.id()))
        .build();
  }

  private UserDto tryAuthenticateUser(Long id) {
    try {
      return userService.findById(id);
    } catch (ResourceNotFoundException _) {
      throw new AuthFailedException();
    }
  }

  private JWTVerifier createRefreshTokenJwtVerifier() {
    return JWT.require(Algorithm.HMAC256(getRefreshSecretKey()))
        .withIssuer(issuer)
        .withClaim("type", "refresh")
        .build();
  }

  private JWTVerifier createAccessTokenJwtVerifier() {
    return JWT.require(Algorithm.HMAC256(getAccessSecretKey()))
        .withIssuer(issuer)
        .withClaimPresence(USER_SERVICE_ID_CLAIM)
        .withClaimPresence(USER_ROLES_CLAIM)
        .withClaim("type", "access")
        .build();
  }

  public void validateAccessToken(String accessToken) throws TokenException {
    try {
      createAccessTokenJwtVerifier().verify(accessToken);
    } catch (JWTVerificationException e) {
      throw TokenException.fromJwtException(e);
    }
  }

  private byte[] getAccessSecretKey() {
    return System.getenv("JWT_ACCESS_KEY").getBytes();
  }

  private byte[] getRefreshSecretKey() {
    return System.getenv("JWT_REFRESH_KEY").getBytes();
  }

  private int getExpiration() {
    return Integer.parseUnsignedInt(System.getenv("JWT_EXPIRATION"));
  }

  private int getRefreshExpiration() {
    return Integer.parseUnsignedInt(System.getenv("JWT_REFRESH_EXPIRATION"));
  }

}
