package com.innowise.auth.test.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.innowise.auth.model.AuthConstants;
import com.innowise.auth.model.JwtUserDetails;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Random;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.GrantedAuthority;

@UtilityClass
public class TestJwtTokenProvider {

  private static final Random random = new SecureRandom();

  public String genRandomSignedAccessToken(JwtUserDetails userDetails) {
    byte[] signKey = new byte[32];
    random.nextBytes(signKey);
    return JWT.create()
        .withExpiresAt(Instant.from(ZonedDateTime.now().plusHours(1)))
        .withIssuer("test")
        .withSubject(userDetails.id().toString())
        .withClaim(AuthConstants.USER_SERVICE_ID_CLAIM, userDetails.userId().toString())
        .withArrayClaim(AuthConstants.USER_ROLES_CLAIM, userDetails.authorities().stream()
            .map(GrantedAuthority::getAuthority).toArray(String[]::new))
        .withClaim("type", "access")
        .sign(Algorithm.HMAC256(signKey));
  }

}
