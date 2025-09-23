package com.innowise.userservice.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

public record UserCreateRequestDto(
    @NotBlank(message = "Name is required")
    @Size(min = 3, message = "Name must be at least 3 characters long")
    String name,

    @NotBlank(message = "Surname is required")
    @Size(min = 3, message = "Surname must be at least 3 characters long")
    String surname,

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    LocalDate birthDate,

    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    String email
) {

}
