package com.innowise.auth.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConstants {

  public static final String USER_ROLES_CLAIM = "realm_access.roles";

  public static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
  public static final String AUTH_SCHEME = "Bearer ";

}
