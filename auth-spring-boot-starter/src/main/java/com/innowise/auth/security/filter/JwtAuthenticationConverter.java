package com.innowise.auth.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.innowise.auth.model.AuthConstants;
import com.innowise.auth.model.JwtUserDetails;
import com.innowise.auth.security.token.LoginRolesJwtAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationConverter;

public class JwtAuthenticationConverter implements AuthenticationConverter {

  @Override
  public @Nullable Authentication convert(HttpServletRequest request) {
    var authHeader = request.getHeader(AuthConstants.AUTH_HEADER);
    if (authHeader == null) {
      return null;
    }
    if (!authHeader.startsWith(AuthConstants.AUTH_SCHEME)) {
      throw new BadCredentialsException("Wrong auth scheme.");
    }
    return extractUserDetails(JWT.decode(authHeader))
        .map(userDetails -> new LoginRolesJwtAuthenticationToken(userDetails, authHeader))
        .orElseThrow(() -> new BadCredentialsException("JWT is malformed."));
  }

  private Optional<JwtUserDetails> extractUserDetails(DecodedJWT jwt) {
    var id = jwt.getSubject();
    var userId = Optional.ofNullable(jwt.getClaim(AuthConstants.USER_SERVICE_ID_CLAIM))
        .map(Claim::asLong)
        .orElse(null);
    var roles = extractAuthorities(jwt);
    if (id == null || id.isBlank() || userId == null || roles.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(JwtUserDetails.builder()
        .id(Long.parseUnsignedLong(id))
        .userId(userId)
        .login(id)
        .authorities(roles)
        .build()
    );
  }

  private Collection<GrantedAuthority> extractAuthorities(DecodedJWT jwt) {
    var rolesClaim = jwt.getClaims().get(AuthConstants.USER_ROLES_CLAIM).asList(String.class);
    if (rolesClaim != null) {
      return rolesClaim.stream()
          .map(Object::toString)
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toUnmodifiableList());
    }
    return List.of();
  }
}
