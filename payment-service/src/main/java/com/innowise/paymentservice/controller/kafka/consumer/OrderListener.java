package com.innowise.paymentservice.controller.kafka.consumer;

import com.innowise.common.model.event.OrderCreatedEvent;
import com.innowise.paymentservice.service.EventService;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
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
import org.springframework.validation.annotation.Validated;

@Component
@Slf4j
@RequiredArgsConstructor
@NullMarked
@Validated
@KafkaListener(
    topics = "${spring.kafka.topics.orders.name}",
    containerFactory = "concurrentKafkaListenerContainerFactory"
)
public class OrderListener {

  private final PaymentService paymentService;
  private final EventService eventService;

  @KafkaHandler
  public void consumeOrderCreatedEvent(
      @Payload @Valid OrderCreatedEvent event,
      @Header(KafkaHeaders.GROUP_ID) String groupId,
      Acknowledgment acknowledgment
  ) {
    log.info("Received {}", event);
    if (eventService.isEventProcessed(groupId, event.getEventId().toString())) {
      log.info("Skip OrderCreatedEvent{id={}} (had already processed earlier", event.getEventId());
      return;
    }
    var payment = paymentService.create(event.getOrder());
    paymentService.processPayment(payment.id());
    eventService.saveProcessedEvent(groupId, event.getEventId().toString());
    acknowledgment.acknowledge();
  }

}
