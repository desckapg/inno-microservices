package com.innowise.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

  USER("USER"),
  MANAGER("MANAGER"),
  ADMIN("ADMIN"),
  SUPER_ADMIN("SUPER_ADMIN");

  private final String authority;

}
