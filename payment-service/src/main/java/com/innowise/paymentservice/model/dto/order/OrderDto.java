package com.innowise.paymentservice.model.dto.order;

import com.innowise.paymentservice.model.dto.orderitem.OrderItemDto;
import com.innowise.paymentservice.model.dto.user.UserDto;
import java.io.Serializable;
import java.util.List;

public record OrderDto(

    Long id,

    UserDto user,

    Status status,

    List<OrderItemDto> orderItems

) implements Serializable {

  public enum Status {
    NEW,
    CANCELLED,
    DELIVERED,
    PROCESSING,
    SHIPPED
  }

}
