package com.innowise.paymentservice.service;


import com.innowise.common.model.dto.order.OrderDto;
import com.innowise.common.model.dto.payment.PaymentDto;

public interface PaymentService {

  PaymentDto create(OrderDto orderDto);

  PaymentDto processPayment(String id);

}
