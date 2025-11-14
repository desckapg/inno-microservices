package com.innowise.paymentservice.controller.kafka.producer;

import com.innowise.common.model.enums.PaymentStatus;
import com.innowise.common.model.dto.payment.PaymentDto;
import com.innowise.common.model.event.PaymentCreatedEvent;
import com.innowise.common.model.event.PaymentStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@NullMarked
@Component
public class PaymentProducer {

  private static final String TOPIC = "queuing.payment_service.payments";

  private final KafkaTemplate<String, String> kafkaTemplate;

  public void sendPaymentCreated(PaymentDto payment) {
    sendMessage(new PaymentCreatedEvent(payment), String.valueOf(payment.orderId()));
  }

  public void sendPaymentStatusUpdated(String id, Long orderId, PaymentStatus previousStatus,
      PaymentStatus newStatus) {
    sendMessage(new PaymentStatusUpdatedEvent(id, orderId, previousStatus, newStatus), String.valueOf(orderId));
  }

  private <T> void sendMessage(T payload, String key) {
    kafkaTemplate.send(MessageBuilder
        .withPayload(payload)
        .setHeader(KafkaHeaders.TOPIC, TOPIC)
        .setHeader(KafkaHeaders.KEY, key)
        .build()
    );
    log.info("{} (Event={}) sent to topic: {}", payload.getClass().getSimpleName(), payload, TOPIC);
  }

}
