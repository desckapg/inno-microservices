package com.innowise.authservice.model.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserConstraints {

  public interface BaseAuth {}

  public interface FindAuth extends BaseAuth {}

  public interface BaseInfo {}

  public interface FindInfo extends BaseInfo {}

  public interface Register extends BaseAuth, BaseInfo, CredentialsConstraints.Register {}

}
