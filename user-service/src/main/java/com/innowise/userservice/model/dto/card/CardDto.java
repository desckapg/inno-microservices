package com.innowise.userservice.model.dto.card;

import com.innowise.userservice.validation.group.OnCreate;
import com.innowise.userservice.validation.group.OnUpdate;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record CardDto(

    @Null(groups = OnCreate.class,
        message = "Id must be null for new cards"
    )
    Long id,

    @NotBlank(message = "Card number is required",
        groups = {OnCreate.class, OnUpdate.class}
    )
    @Pattern(
        regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
        message = "Card number must be in the format XXXX-XXXX-XXXX-XXXX",
        groups = {OnCreate.class, OnUpdate.class}
    )
    String number,

    @NotBlank(message = "Card holder is required",
        groups = {OnCreate.class, OnUpdate.class}
    )
    @Size(min = 3, message = "Card holder must be at least 3 characters long",
        groups = {OnCreate.class, OnUpdate.class}
    )
    String holder,

    @NotNull(message = "Expiration date is required",
        groups = {OnCreate.class, OnUpdate.class}
    )
    @Future(message = "Expiration date must be in the future",
        groups = {OnCreate.class, OnUpdate.class}
    )
    LocalDate expirationDate,

    Long userId
) {

}
