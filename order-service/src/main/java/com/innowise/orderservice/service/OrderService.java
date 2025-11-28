package com.innowise.orderservice.service;

import com.innowise.common.model.dto.payment.PaymentDto;
import com.innowise.common.model.enums.PaymentStatus;
import com.innowise.orderservice.model.dto.order.OrderDto;
import com.innowise.orderservice.model.dto.order.OrderSpecsDto;
import java.util.List;

public interface OrderService {

  OrderDto findById(Long id);

  List<OrderDto> findAll(OrderSpecsDto orderSpecsDto);

  OrderDto create(OrderDto orderDto);

  OrderDto update(Long id, OrderDto orderDto);

  void delete(Long id);

  void processPaymentCreation(PaymentDto payment);

  void processPaymentUpdate(Long orderId, PaymentStatus newStatus);

}
