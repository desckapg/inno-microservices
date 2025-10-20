package com.innowise.orderservice.model.dto.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.orderservice.model.dto.ItemsConstraints;
import com.innowise.orderservice.model.dto.OrdersConstraints;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemDto(

    @NotNull(
        groups = {
            OrdersConstraints.Create.class
        },
        message = "Id must be provided"
    )
    @JsonView({OrdersConstraints.Create.class})
    Long id,

    @NotBlank(
        groups = {
            ItemsConstraints.Update.class
        },
        message = "Name must be provided"
    )
    String name,

    @NotNull(
        groups = {
            ItemsConstraints.Create.class
        },
        message = "Price must be provided"
    )
    @DecimalMin(
        groups = {
            ItemsConstraints.Create.class
        },
        value = "0.0",
        message = "Price must be greater than 0 or equal"
    )
    BigDecimal price

) implements Serializable {

}
