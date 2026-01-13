package com.innowise.auth.security.filter;

import com.innowise.auth.security.provider.AuthTokenProvider;
import com.innowise.auth.security.token.LoginRolesJwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
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
    setAuthenticationConverter(new JwtAuthenticationConverter());
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
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException, ServletException {
    super.successfulAuthentication(request, response, chain, authResult);
    chain.doFilter(request, response);
  }
}
