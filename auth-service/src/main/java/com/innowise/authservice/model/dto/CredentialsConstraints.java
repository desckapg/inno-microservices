package com.innowise.authservice.model.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CredentialsConstraints {

  public interface Base {}

  public interface Register extends Base {}

}
