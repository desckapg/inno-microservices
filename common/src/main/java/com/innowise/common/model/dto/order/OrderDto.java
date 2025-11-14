package com.innowise.common.model.dto.order;

import com.innowise.common.model.enums.OrderStatus;
import com.innowise.common.model.dto.orderitem.OrderItemDto;
import com.innowise.common.model.dto.user.UserDto;
import java.io.Serializable;
import java.util.List;

public record OrderDto(

    Long id,

    UserDto user,

    OrderStatus status,

    List<OrderItemDto> orderItems

) implements Serializable {

}
