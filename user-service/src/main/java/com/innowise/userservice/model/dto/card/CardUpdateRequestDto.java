package com.innowise.userservice.model.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.io.Serializable;
import java.time.LocalDate;

public record CardUpdateRequestDto(
    @NotBlank String number,
    @NotBlank String holder,
    @NotNull LocalDate expirationDate
) implements Serializable {

}
