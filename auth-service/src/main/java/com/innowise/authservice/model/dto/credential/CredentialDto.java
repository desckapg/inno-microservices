package com.innowise.authservice.model.dto.credential;

import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.authservice.model.dto.CredentialsConstraints;
import com.innowise.authservice.model.dto.UserConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@JsonView(UserConstraints.BaseAuth.class)
public record CredentialDto(

    @NotBlank(
        groups = UserConstraints.BaseAuth.class,
        message = "Login must be provided"
    )
    @Size(
        groups = CredentialsConstraints.Register.class,
        min = 3,
        max = 24,
        message = "Login length must be between 3 and 24"
    )
    String login,

    @NotBlank(
        groups = UserConstraints.BaseAuth.class,
        message = "Password must be provided"
    )
    @Pattern(
        groups = CredentialsConstraints.Register.class,
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
        message = "Password must contain at least 8 characters, one digit and one letter"
    )
    String password

) {

}
