package com.innowise.userservice.model.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

public record CardCreateRequestDto(
    @NotBlank String number,
    @NotBlank String holder,
    @NotNull @Future LocalDate expirationDate,
    @NotNull Long user_id
) implements Serializable {

}
