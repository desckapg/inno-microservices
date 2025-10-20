package com.innowise.auth.security.token;

import com.innowise.auth.model.JwtUserDetails;
import java.io.Serial;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

@NullMarked
public class LoginRolesJwtAuthenticationToken extends AbstractAuthenticationToken {

  @Serial
  private static final long serialVersionUID = 7689798587912944809L;

  @Getter
  private final String jwtToken;
  private final JwtUserDetails user;

  public LoginRolesJwtAuthenticationToken(JwtUserDetails user, String jwtToken) {
    super(user.getAuthorities());
    this.user = user;
    this.jwtToken = jwtToken;
    super.setAuthenticated(true);
  }

  @Override
  public @Nullable Object getCredentials() {
    return user.getPassword();
  }

  @Override
  public JwtUserDetails getPrincipal() {
    return user;
  }

  @Override
  public Builder<?> toBuilder() {
    return new Builder<>(this);
  }

  public static class Builder<B extends LoginRolesJwtAuthenticationToken.Builder<B>> extends
      AbstractAuthenticationBuilder<B> {

    private JwtUserDetails user;
    private final String jwtToken;

    protected Builder(LoginRolesJwtAuthenticationToken token) {
      super(token);
      this.user = token.user;
      this.jwtToken = token.jwtToken;
    }

    @Override
    @SuppressWarnings("unchecked")
    public B principal(@Nullable Object principal) {
      Assert.notNull(principal, "principal cannot be null");
      this.user = (JwtUserDetails) principal;
      return (B) this;
    }

    @Override
    public @Nullable B credentials(@Nullable Object credentials) {
      return null;
    }

    @Override
    public LoginRolesJwtAuthenticationToken build() {
      return new LoginRolesJwtAuthenticationToken(user, jwtToken);
    }

  }
}
