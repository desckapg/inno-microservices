package com.innowise.auth.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.innowise.auth.model.AuthConstants;
import com.innowise.auth.model.JwtUserDetails;
import com.innowise.auth.security.provider.AuthTokenProvider;
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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

@NullMarked
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  public JwtAuthenticationFilter(
      RequestMatcher requiresAuthenticationRequestMatcher,
      AuthenticationManager authenticationManager,
      AuthTokenProvider authTokenProvider
  ) {
    super(requiresAuthenticationRequestMatcher, authenticationManager);
    setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {
      @Override
      public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
          Authentication authentication) {
        authTokenProvider.set((LoginRolesJwtAuthenticationToken) authentication);
      }

      @Override
      public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
          FilterChain chain, Authentication authentication) throws ServletException, IOException {
        authTokenProvider.set((LoginRolesJwtAuthenticationToken) authentication);
        chain.doFilter(request, response);
      }
    });
  }

  @Override
  public @Nullable Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {
    String rawJwt = obtainJwtToken(request);
    if (rawJwt == null) {
      throw new BadCredentialsException("JWT isn't provided");
    }
    var jwt = JWT.decode(rawJwt);

    var userDetails = extractUserDetails(jwt);

    if (userDetails == null) {
      throw new BadCredentialsException("JWT is malformed");
    }

    var authentication = new LoginRolesJwtAuthenticationToken(userDetails, rawJwt);
    setDetails(request, authentication);
    return this.getAuthenticationManager().authenticate(authentication);
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException, ServletException {
    super.successfulAuthentication(request, response, chain, authResult);
    chain.doFilter(request, response);
  }

  protected void setDetails(HttpServletRequest request,
      LoginRolesJwtAuthenticationToken authRequest) {
    authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
  }

  private @Nullable String obtainJwtToken(HttpServletRequest request) {
    var authHeader = request.getHeader(AuthConstants.AUTH_HEADER);
    if (authHeader == null || !authHeader.startsWith(AuthConstants.AUTH_SCHEME)) {
      return null;
    }
    return authHeader.substring(AuthConstants.AUTH_SCHEME.length());
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
