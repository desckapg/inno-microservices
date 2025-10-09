package com.innowise.authservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Credentials {

  @Column(name = "login", nullable = false, unique = true)
  private String login;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

}
