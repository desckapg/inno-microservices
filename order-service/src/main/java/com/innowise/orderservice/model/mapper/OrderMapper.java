package com.innowise.orderservice.model.mapper;

import com.innowise.common.model.dto.user.UserDto;
import com.innowise.orderservice.model.dto.order.OrderDto;
import com.innowise.orderservice.model.entity.Order;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    injectionStrategy = org.mapstruct.InjectionStrategy.CONSTRUCTOR,
    uses = {OrderItemMapper.class}
)
public interface OrderMapper {

  @Mapping(target = "user", expression = "java(userDto)")
  @Mapping(target = "orderItems", qualifiedByName = "toDtoIdOnly")
  OrderDto toDto(Order order, @Context UserDto userDto);

  com.innowise.common.model.dto.order.OrderDto toExternalDto(OrderDto orderDto);

  @Mapping(target = "user", expression = "java(userDto)")
  OrderDto toFullDto(Order order, @Context UserDto userDto);

  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Order toEntity(OrderDto orderDto);

  @AfterMapping
  default void linkOrderItems(@MappingTarget Order order) {
    order.getOrderItems().forEach(item -> item.setOrder(order));
  }


}
