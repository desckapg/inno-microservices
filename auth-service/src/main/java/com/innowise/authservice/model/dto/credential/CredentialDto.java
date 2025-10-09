package com.innowise.authservice.model.dto.credential;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CredentialDto(

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
    String password

) {

}
