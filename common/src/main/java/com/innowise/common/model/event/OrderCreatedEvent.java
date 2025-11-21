package com.innowise.common.model.event;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.innowise.common.model.dto.order.OrderDto;
import jakarta.validation.Valid;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OrderCreatedEvent extends BaseEvent implements Serializable {

  @Serial
  private static final long serialVersionUID = 1629083144347308904L;

  @JsonUnwrapped
  @Valid
  private final OrderDto order;

  public OrderCreatedEvent(OrderDto order) {
    this.order = order;
  }
}
