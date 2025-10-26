package com.innowise.auth.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.innowise.auth.model.AuthConstants;
import com.innowise.auth.model.JwtUserDetails;
import com.innowise.auth.security.provider.AuthTokenProvider;
import com.innowise.auth.security.provider.AuthTokenProviderInterceptor;
import com.innowise.auth.security.token.LoginRolesJwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@NullMarked
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final AuthTokenProvider authTokenProvider;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(AuthConstants.AUTH_HEADER);

    if (authHeader == null || !authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = authHeader.substring(AuthConstants.BEARER_PREFIX.length()).trim();
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

        var authentication = new LoginRolesJwtAuthenticationToken(userDetails, token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        authTokenProvider.set(authentication);
      } catch (JWTVerificationException _) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private @Nullable JwtUserDetails extractUserDetails(DecodedJWT jwt) {
    var id = jwt.getSubject();
    var userId = Optional.ofNullable(jwt.getClaim(AuthConstants.USER_SERVICE_ID_CLAIM))
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
