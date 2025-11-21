package com.innowise.common.model.dto.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public record ItemDto(

    @NotNull(
        message = "Id must be provided"
    )
    @Min(
        value = 1,
        message = "Id must be not less than 1"
    )
    Long id,

    @NotBlank(
        message = "Name must be provided"
    )
    String name,

    @NotNull(
        message = "Price must be provided"
    )
    @DecimalMin(
        value = "0.0",
        inclusive = false,
        message = "Price must be greater than 0"
    )
    BigDecimal price

) implements Serializable {

}
