package com.innowise.auth.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConstants {

  public static final String USER_SERVICE_ID_CLAIM = "userId";
  public static final String USER_ROLES_CLAIM = "roles";

  public static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
  public static final String BEARER_PREFIX = "Bearer ";

}
