package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.dto.order.OrderDto;
import com.innowise.paymentservice.model.dto.payment.PaymentDto;

public interface PaymentService {

  PaymentDto create(OrderDto orderDto);

  PaymentDto processPayment(PaymentDto paymentDto);

}
