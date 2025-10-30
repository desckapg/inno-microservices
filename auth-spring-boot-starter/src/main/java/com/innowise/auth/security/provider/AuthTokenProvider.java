package com.innowise.auth.security.provider;

import com.innowise.auth.security.token.LoginRolesJwtAuthenticationToken;

public class AuthTokenProvider {

  private static final ThreadLocal<LoginRolesJwtAuthenticationToken> DELEGATE = new ThreadLocal<>();

  public void set(LoginRolesJwtAuthenticationToken authToken) {
    DELEGATE.set(authToken);
  }

  public LoginRolesJwtAuthenticationToken get() {
    var authToken = DELEGATE.get();
    if (authToken == null) {
      throw new IllegalStateException("AuthTokenProvider has not been initialized");
    }
    return authToken;
  }

  public void clear() {
    DELEGATE.remove();
  }

}
