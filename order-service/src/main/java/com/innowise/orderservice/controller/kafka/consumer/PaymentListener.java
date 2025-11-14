package com.innowise.orderservice.controller.kafka.consumer;

import com.innowise.common.model.event.PaymentCreatedEvent;
import com.innowise.common.model.event.PaymentStatusUpdatedEvent;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@NullMarked
@KafkaListener(
    topics = "queuing.payment_service.payments",
    containerFactory = "kafkaListenerContainerFactory",
    groupId = "payment-processing-group"
)
public class PaymentListener {

  private final OrderService orderService;

  @KafkaHandler
  public void consumePaymentCreatedEvent(PaymentCreatedEvent event) {
    log.info("Received PaymentCreatedEvent (Event={})", event);
    orderService.processPaymentCreation(event.getPayment());
  }

  @KafkaHandler
  public void consumePaymentStatusUpdatedEvent(PaymentStatusUpdatedEvent event) {
    log.info("Received PaymentStatusUpdatedEvent (Event={})", event);
    orderService.processPaymentUpdate(event.getOrderId(), event.getNewStatus());
  }

}
