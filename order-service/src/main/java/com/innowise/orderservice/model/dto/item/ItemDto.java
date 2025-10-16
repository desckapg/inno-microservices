package com.innowise.orderservice.model.dto.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public record ItemDto(

    @NotBlank(
        message = "Name must be provided"
    )
    String name,

    @NotNull(
        message = "Price must be provided"
    )
    @DecimalMin(
        value = "0.0",
        message = "Price must be greater than 0 or equal"
    )
    BigDecimal price

) implements Serializable {

}
