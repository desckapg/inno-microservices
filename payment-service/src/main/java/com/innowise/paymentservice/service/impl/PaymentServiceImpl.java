package com.innowise.paymentservice.service.impl;

import com.innowise.common.exception.ExternalApiException;
import com.innowise.common.exception.ResourceNotFoundException;
import com.innowise.common.model.dto.order.OrderDto;
import com.innowise.common.model.dto.payment.PaymentDto;
import com.innowise.common.model.enums.PaymentStatus;
import com.innowise.paymentservice.controller.kafka.producer.PaymentProducer;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.model.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import com.innowise.paymentservice.service.client.StripeClient;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

@NullMarked
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final StripeClient stripeClient;
  private final PaymentMapper paymentMapper;
  private final PaymentProducer paymentProducer;

  @Override
  public PaymentDto create(OrderDto orderDto) {
    var paymentDto = PaymentDto.builder()
        .orderId(orderDto.id())
        .userId(orderDto.user().id())
        .amount(calculateOPaymentAmount(orderDto))
        .timestamp(Instant.now())
        .status(PaymentStatus.PENDING)
        .build();
    var savedPaymentEntity = paymentRepository.save(paymentMapper.toEntity(paymentDto));
    var savedPaymentDto = paymentMapper.toDto(savedPaymentEntity);
    log.info("{} created", savedPaymentDto);
    paymentProducer.sendPaymentCreated(savedPaymentDto);
    return savedPaymentDto;
  }

  private BigDecimal calculateOPaymentAmount(OrderDto orderDto) {
    return orderDto.orderItems().stream()
        .map(oi -> oi.item().price().multiply(BigDecimal.valueOf(oi.quantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public PaymentDto processPayment(String id) {
    var payment = paymentRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.byId("Payment", id));
    log.info("Processing payment{id={}}", payment.getId());

    updatePaymentStatus(payment, PaymentStatus.PROCESSING);
    PaymentStatus afterPaymentStatus;
    try {
      if (stripeClient.processPayment()[0] % 2 == 0) {
        afterPaymentStatus = PaymentStatus.SUCCEEDED;
      } else {
        afterPaymentStatus = PaymentStatus.FAILED;
      }
    } catch (ExternalApiException _) {
      afterPaymentStatus = PaymentStatus.FAILED;
    }
    updatePaymentStatus(payment, afterPaymentStatus);

    return paymentMapper.toDto(payment);
  }

  private void updatePaymentStatus(Payment payment, PaymentStatus newStatus) {
    var oldStatus = payment.getStatus();
    payment.setStatus(newStatus);
    paymentRepository.save(payment);
    paymentProducer.sendPaymentStatusUpdated(
        payment.getId(),
        payment.getOrderId(),
        oldStatus,
        newStatus
    );

    log.info("Payment{id={}} changed status from {} to {}", payment.getId(), oldStatus,
        newStatus);

  }
}
