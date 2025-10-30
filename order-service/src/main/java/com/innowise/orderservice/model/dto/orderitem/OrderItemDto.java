package com.innowise.orderservice.model.dto.orderitem;

import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.orderservice.model.dto.OrdersConstraints;
import com.innowise.orderservice.model.dto.item.ItemDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Builder;

@Builder
@JsonView(OrdersConstraints.Base.class)
public record OrderItemDto(

    @NotNull(
        message = "Item must be provided"
    )
    @Valid
    ItemDto item,

    @NotNull(
        message = "Quantity must be provided"
    )
    @Min(
        value = 1,
        message = "Quantity must greater than 0"
    )
    Integer quantity

) implements Serializable {

}
