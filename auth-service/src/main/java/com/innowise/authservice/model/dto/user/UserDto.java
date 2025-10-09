package com.innowise.authservice.model.dto.user;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@NullMarked
public record UserDto(
    Long id,
    String login,
    List<String> roles,
    Long userId
) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(SimpleGrantedAuthority::new)
        .toList();
  }

  @Override
  public @Nullable String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return login;
  }
}
