package com.innowise.common.model.dto.orderitem;

import com.innowise.common.model.dto.item.ItemDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public record OrderItemDto(

    @Valid
    ItemDto item,

    @NotNull(
        message = "Quantity must be provided"
    )
    @Min(
        value = 1,
        message = "Quantity must be not less than 1"
    )
    Integer quantity

) implements Serializable {

}
