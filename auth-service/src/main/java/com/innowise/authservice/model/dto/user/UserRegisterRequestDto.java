package com.innowise.authservice.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserRegisterRequestDto(

  @NotBlank(
    message = "Login must be provided"
  )
  @Size(
    min = 3,
    max = 24,
    message = "Login length must be between 3 and 24"
  )
  String login,

  @NotBlank(
    message = "Password must be provided"
  )
  @Pattern(
    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
    message = "Password must contain at least 8 characters, one digit and one letter"
  )
  String password,

  @NotBlank(
    message = "Name must be provided"
  )
  String name,

  @NotBlank(
    message = "Surname must be provided"
  )
  String surname,

  @NotNull(
    message = "Birth date must be provided"
  )
  LocalDate birthDate,

  @NotBlank(
    message = "Email must be provided"
  )
  @Email(
    message = "Email must be valid"
  )
  String email
) {

}
