package com.innowise.paymentservice.controller.kafka.consumer;

import com.innowise.common.model.event.OrderCreatedEvent;
import com.innowise.paymentservice.service.PaymentService;
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
    topics = "queuing.order_service.orders",
    containerFactory = "kafkaListenerContainerFactory",
    groupId = "order-processing-group"
)
public class OrderListener {

  private final PaymentService paymentService;

  @KafkaHandler
  public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
    log.info("Received OrderCreatedEvent {}", event);
    var payment = paymentService.create(event.getOrder());
    paymentService.processPayment(payment.id());
  }

}
