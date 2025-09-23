package com.innowise.userservice.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

public record UserUpdateRequestDto(
    @NotBlank @Size(min = 3) String name,
    @NotBlank @Size(min = 3) String surname,
    @Email @NotNull String email,
    @Past @NotNull LocalDate birthDate
    ) implements Serializable {

}
