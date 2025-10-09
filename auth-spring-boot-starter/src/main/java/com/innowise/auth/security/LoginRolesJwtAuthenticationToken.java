package com.innowise.auth.security;

import com.innowise.auth.model.JwtUserDetails;
import java.io.Serial;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

public class LoginRolesJwtAuthenticationToken extends AbstractAuthenticationToken {

  @Serial
  private static final long serialVersionUID = 7689798587912944809L;

  private final JwtUserDetails user;

  public LoginRolesJwtAuthenticationToken(JwtUserDetails user) {
    super(user.getAuthorities());
    this.user = user;
    super.setAuthenticated(true);
  }

  @Override
  public @Nullable Object getCredentials() {
    return user.getPassword();
  }

  @Override
  public @Nullable UserDetails getPrincipal() {
    return user;
  }

  @Override
  public @NonNull Builder<?> toBuilder() {
    return new Builder<>(this);
  }

  public static class Builder<B extends LoginRolesJwtAuthenticationToken.Builder<B>> extends
      AbstractAuthenticationBuilder<@NonNull B> {

    private JwtUserDetails user;

    protected Builder(LoginRolesJwtAuthenticationToken token) {
      super(token);
      this.user = token.user;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B principal(@Nullable Object principal) {
      Assert.notNull(principal, "principal cannot be null");
      this.user = (JwtUserDetails) principal;
      return (B) this;
    }

    @Override
    public B credentials(@Nullable Object credentials) {
      return null;
    }

    @Override
    public @NonNull LoginRolesJwtAuthenticationToken build() {
      return new LoginRolesJwtAuthenticationToken(user);
    }

  }
}
