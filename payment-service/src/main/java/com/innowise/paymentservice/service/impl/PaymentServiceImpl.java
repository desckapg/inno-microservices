package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.model.dto.order.OrderDto;
import com.innowise.paymentservice.model.dto.payment.PaymentDto;
import com.innowise.paymentservice.model.dto.payment.PaymentDto.Status;
import com.innowise.paymentservice.model.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import com.innowise.paymentservice.service.client.PaymentSystemClient;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@NullMarked
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentSystemClient paymentSystemClient;
  private final PaymentMapper paymentMapper;
  private final KafkaTemplate<String, PaymentDto> paymentKafkaTemplate;

  @Override
  @Transactional
  public PaymentDto create(OrderDto orderDto) {
    log.info("Creating a new payment from order={}", orderDto);
    var paymentDto = PaymentDto.builder()
        .orderId(orderDto.id())
        .userId(orderDto.user().id())
        .amount(calculateOPaymentAmount(orderDto))
        .timestamp(ZonedDateTime.now())
        .status(Status.PENDING)
        .build();
    log.debug("Saving payment={} to database", paymentDto);
    var savedPayment = paymentRepository.save(paymentMapper.toEntity(paymentDto));
    log.info("Payment={} created", paymentDto);
    return paymentMapper.toDto(savedPayment);
  }

  private BigDecimal calculateOPaymentAmount(OrderDto orderDto) {
    return orderDto.orderItems().stream()
        .map(oi -> oi.item().price().multiply(BigDecimal.valueOf(oi.quantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  @Transactional
  public PaymentDto processPayment(PaymentDto paymentDto) {
    log.info("Processing paymentDto={}", paymentDto);
    PaymentDto.PaymentDtoBuilder builder = PaymentDto.builder()
        .id(paymentDto.id())
        .amount(paymentDto.amount())
        .orderId(paymentDto.orderId())
        .userId(paymentDto.userId())
        .timestamp(paymentDto.timestamp());

    log.debug("Updating paymentDto={} to database", paymentDto);
    PaymentDto.Status status = PaymentDto.Status.PROCESSING;
    var payment = paymentMapper.toEntity(builder.status(status).build());
    paymentRepository.save(payment);
    log.info("Payment={} changed status to {}", paymentDto, status);

    if (paymentSystemClient.processPayment() % 2 == 0) {
      status = Status.SUCCEEDED;
    } else {
      status = Status.FAILED;
    }
    log.info("Payment={} changed status to {}", paymentDto, status);
    var processedPaymentDto = builder.status(status).build();
    log.debug("Updating payment={}", processedPaymentDto);
    paymentRepository.save(paymentMapper.toEntity(processedPaymentDto));
    log.info("Payment={} has processed", processedPaymentDto);

    return processedPaymentDto;
  }
}
