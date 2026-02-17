package com.innowise.auth.model;

import java.io.Serializable;
import java.util.Collection;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;

@Builder
public record JwtUserDetails(
    String id,
    Collection<? extends GrantedAuthority> authorities) implements Serializable {}