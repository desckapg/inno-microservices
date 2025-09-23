package com.innowise.userservice.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

public record UserCreateRequestDto(
    @NotBlank @Size(min = 3) String name,
    @NotBlank @Size(min = 3) String surname,
    @NotNull @Past LocalDate birthDate,
    @NotNull @Email String email
) implements Serializable {

}
