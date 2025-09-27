package com.innowise.userservice.model.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.validation.group.OnCreate;
import com.innowise.userservice.validation.group.OnUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record UserDto(

    @Null(groups = OnCreate.class,
        message = "Id must be null for new users"
    )
    Long id,

    @NotBlank(groups = {OnCreate.class, OnUpdate.class},
        message = "Name is required"
    )
    @Size(groups = {OnCreate.class, OnUpdate.class},
        min = 3,
        message = "Name must be at least 3 characters long"
    )
    String name,

    @NotBlank(message = "Surname is required")
    @Size(min = 3,
        message = "Surname must be at least 3 characters long"
    )
    String surname,

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    LocalDate birthDate,

    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<CardDto> cards
) {

}
