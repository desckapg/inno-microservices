package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.order.OrderDto;
import com.innowise.orderservice.model.dto.order.OrderSpecsDto;
import java.util.List;
import org.jspecify.annotations.Nullable;

public interface OrderService {

  OrderDto findById(Long id);

  List<OrderDto> findAll(@Nullable OrderSpecsDto orderSpecsDto);

  OrderDto create(OrderDto orderDto);

  OrderDto update(Long id, OrderDto orderDto);

  void delete(Long id);

}
