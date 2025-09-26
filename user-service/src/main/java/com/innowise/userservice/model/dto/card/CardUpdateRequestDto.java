package com.innowise.userservice.model.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CardUpdateRequestDto(
    @NotBlank(message = "Card number is required")
    @Pattern(
        regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
        message = "Card number must be in the format XXXX-XXXX-XXXX-XXXX"
    )
    String number,

    @NotBlank(message = "Card holder is required")
    @Size(min = 3, message = "Card holder must be at least 3 characters long")
    String holder,

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    LocalDate expirationDate
)  {

}
