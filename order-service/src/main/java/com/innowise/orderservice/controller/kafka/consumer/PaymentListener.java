package com.innowise.orderservice.controller.kafka.consumer;

import com.innowise.common.model.event.PaymentCreatedEvent;
import com.innowise.common.model.event.PaymentStatusUpdatedEvent;
import com.innowise.orderservice.service.EventService;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@NullMarked
@KafkaListener(
    topics = "${spring.kafka.topics.payments.name}",
    containerFactory = "kafkaListenerContainerFactory"
)
public class PaymentListener {

  private final OrderService orderService;
  private final EventService eventService;

  @KafkaHandler
  public void consumePaymentCreatedEvent(
      @Payload PaymentCreatedEvent event,
      @Header(KafkaHeaders.GROUP_ID) String groupId,
      Acknowledgment acknowledgment) {
    log.info("Received PaymentCreatedEvent (Event={})", event);
    if (eventService.isEventProcessed(groupId, event.getEventId().toString())) {
      log.info("Skip PaymentCreatedEvent{id={}} (had already processed earlier)", event.getEventId());
      return;
    }
    orderService.processPaymentCreation(event.getPayment());
    eventService.saveProcessedEvent(groupId, event.getEventId().toString());
    acknowledgment.acknowledge();
  }

  @KafkaHandler
  public void consumePaymentStatusUpdatedEvent(
      @Payload PaymentStatusUpdatedEvent event,
      @Header(KafkaHeaders.GROUP_ID) String groupId,
      Acknowledgment acknowledgment) {
    log.info("Received PaymentStatusUpdatedEvent (Event={})", event);
    if (eventService.isEventProcessed(groupId, event.getEventId().toString())) {
      log.info("Skip PaymentStatusUpdateEvent{id={}} (had already processed earlier)", event.getEventId());
      return;
    }
    orderService.processPaymentUpdate(event.getOrderId(), event.getNewStatus());
    eventService.saveProcessedEvent(groupId, event.getEventId().toString());
    acknowledgment.acknowledge();
  }

}
