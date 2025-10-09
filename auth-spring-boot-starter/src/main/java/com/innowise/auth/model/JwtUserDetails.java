package com.innowise.auth.model;

import java.util.Collection;
import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder
public record JwtUserDetails(
    Long id,
    Long userId,
    String login,
    Collection<? extends GrantedAuthority> authorities) implements UserDetails, CredentialsContainer {

  @Override
  public void eraseCredentials() {
    // Nothing to erase since we don't store credentials
  }

  @Override
  public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public @Nullable String getPassword() {
    return null;
  }

  @Override
  public @NonNull String getUsername() {
    return login;
  }
}
