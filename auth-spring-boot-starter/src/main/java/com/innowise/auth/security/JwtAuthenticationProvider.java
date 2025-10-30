package com.innowise.auth.security;

import com.innowise.auth.security.token.LoginRolesJwtAuthenticationToken;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

@NullMarked
public class JwtAuthenticationProvider implements AuthenticationProvider {

  @Override
  public @Nullable Authentication authenticate(Authentication authentication)
      throws AuthenticationException {
    Assert.isInstanceOf(
        LoginRolesJwtAuthenticationToken.class,
        authentication,
        "JwtAuthenticationProvider only supports LoginRolesJwtAuthenticationToken"
    );
    return authentication;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return LoginRolesJwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
