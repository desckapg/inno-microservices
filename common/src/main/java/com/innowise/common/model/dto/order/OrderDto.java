package com.innowise.common.model.dto.order;

import com.innowise.common.model.enums.OrderStatus;
import com.innowise.common.model.dto.orderitem.OrderItemDto;
import com.innowise.common.model.dto.user.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.ToString;

public record OrderDto(

    @NotNull(
        message = "Id must be provided"
    )
    @Min(
        value = 1,
        message = "Id must be not less than 1"
    )
    Long id,

    @NotNull(
        message = "User must be provided"
    )
    @Valid
    UserDto user,

    @NotNull(
        message = "Order status must be provided"
    )
    OrderStatus status,

    @NotNull(
        message = "Order items must be provided"
    )
    @Valid
    List<OrderItemDto> orderItems

) implements Serializable {

}
