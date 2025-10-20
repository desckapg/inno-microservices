package com.innowise.auth.security.provider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

@NullMarked
@RequiredArgsConstructor
public class AuthTokenProviderInterceptor implements HandlerInterceptor {

  private final AuthTokenProvider authTokenProvider;

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response,
      Object handler, @Nullable Exception ex) {
    authTokenProvider.clear();
  }
}
