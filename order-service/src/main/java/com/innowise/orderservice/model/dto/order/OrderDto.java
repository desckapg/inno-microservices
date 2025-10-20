package com.innowise.orderservice.model.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.orderservice.model.dto.OrdersConstraints;
import com.innowise.orderservice.model.dto.orderitem.OrderItemDto;
import com.innowise.orderservice.model.dto.user.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderDto(

    @JsonView({OrdersConstraints.Find.class})
    Long id,

    @JsonView({OrdersConstraints.Find.class})
    UserDto user,

    @JsonView({
        OrdersConstraints.Find.class,
        OrdersConstraints.Update.class
    })
    Status status,

    @JsonView({
        OrdersConstraints.Create.class,
        OrdersConstraints.Update.class
    })
    @NotNull(
        groups = {
            OrdersConstraints.Create.class
        },
        message = "Order items must be provided"
    )
    @NotEmpty(
        groups = {
            OrdersConstraints.Create.class
        },
        message = "Order items must be provided"
    )
    @Valid
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
