package com.innowise.common.model.dto.card;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;

public record CardDto(

    @NotNull(
        message = "Id must be provided"
    )
    @Min(
        value = 1,
        message = "Id must be not less than 1"
    )
    Long id,

    @NotBlank(
        message = "Number must be provided"
    )
    @Pattern(
        regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
        message = "Card number must be in the format XXXX-XXXX-XXXX-XXXX"
    )
    String number,

    @NotBlank(
        message = "Holder must be provided"
    )
    @Pattern(regexp = "^[A-Z\\s]{2,50}$",
             message = "Holder name must contain only uppercase letters and spaces"
    )
    String holder,

    @NotNull(
        message = "Expiration date must be provided"
    )
    LocalDate expirationDate,

    @NotNull(
        message = "User id must be provided"
    )
    @Min(
        value = 1,
        message = "Id must be not less than 1"
    )
    Long userId

) implements Serializable {

}
