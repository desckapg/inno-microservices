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
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@NullMarked
@KafkaListener(
    topics = PaymentListener.TOPIC,
    containerFactory = "kafkaListenerContainerFactory",
    groupId = PaymentListener.GROUP_ID
)
public class PaymentListener {

  public static final String GROUP_ID = "payment-processing-group";
  public static final String TOPIC = "queuing.payment_service.payments";

  private final OrderService orderService;
  private final EventService eventService;

  @KafkaHandler
  public void consumePaymentCreatedEvent(PaymentCreatedEvent event, Acknowledgment acknowledgment) {
    log.info("Received PaymentCreatedEvent (Event={})", event);
    if (eventService.isEventProcessed(GROUP_ID, event.getEventId().toString())) {
      log.info("Skip PaymentCreatedEvent{id={}} (had already processed earlier)", event.getEventId());
      return;
    }
    orderService.processPaymentCreation(event.getPayment());
    eventService.saveProcessedEvent(GROUP_ID, event.getEventId().toString());
    acknowledgment.acknowledge();
  }

  @KafkaHandler
  public void consumePaymentStatusUpdatedEvent(PaymentStatusUpdatedEvent event, Acknowledgment acknowledgment) {
    log.info("Received PaymentStatusUpdatedEvent (Event={})", event);
    if (eventService.isEventProcessed(GROUP_ID, event.getEventId().toString())) {
      log.info("Skip PaymentStatusUpdateEvent{id={}} (had already processed earlier)", event.getEventId());
      return;
    }
    orderService.processPaymentUpdate(event.getOrderId(), event.getNewStatus());
    eventService.saveProcessedEvent(GROUP_ID, event.getEventId().toString());
    acknowledgment.acknowledge();
  }

}
