package com.innowise.auth.security.interceptor;

import com.innowise.auth.security.token.LoginRolesJwtAuthenticationToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

@NullMarked
@RequiredArgsConstructor
public class JwtAuthInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate template) {
    var authToken = (LoginRolesJwtAuthenticationToken) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
    template.header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken.getJwtToken());
  }

}
