package com.innowise.auth.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.innowise.auth.model.JwtUserDetails;
import com.innowise.auth.security.LoginRolesJwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@NullMarked
public class JwtExtractorFilter extends OncePerRequestFilter {

  private static final String USER_SERVICE_ID_CLAIM = "userId";
  private static final String USER_ROLES_CLAIM = "roles";

  private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(AUTH_HEADER);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(BEARER_PREFIX.length()).trim();
    if (token.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        var jwt = JWT.decode(token);

        var userDetails = extractUserDetails(jwt);

        if (userDetails == null) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }

        var authentication = new LoginRolesJwtAuthenticationToken(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JWTVerificationException _) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private @Nullable JwtUserDetails extractUserDetails(DecodedJWT jwt) {
    var id = jwt.getSubject();
    var userId = Optional.ofNullable(jwt.getClaim(USER_SERVICE_ID_CLAIM))
        .map(Claim::asLong)
        .orElse(null);
    var roles = extractAuthorities(jwt);
    if (id == null || id.isBlank() || userId == null || roles.isEmpty()) {
      return null;
    }
    return JwtUserDetails.builder()
        .id(Long.parseUnsignedLong(id))
        .userId(userId)
        .login(id)
        .authorities(roles)
        .build();
  }

  private Collection<GrantedAuthority> extractAuthorities(DecodedJWT jwt) {
    Object rolesClaim = jwt.getClaims().get(USER_ROLES_CLAIM);
    if (rolesClaim instanceof Collection<?> c) {
      return c.stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toUnmodifiableList());
    }
    return List.of();
  }
}
