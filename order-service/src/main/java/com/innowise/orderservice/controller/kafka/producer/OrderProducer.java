package com.innowise.orderservice.controller.kafka.producer;

import com.innowise.common.model.event.OrderCreatedEvent;
import com.innowise.orderservice.model.dto.order.OrderDto;
import com.innowise.orderservice.model.mapper.OrderMapper;
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
public class OrderProducer {

  private static final String TOPIC = "queuing.order_service.orders";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final OrderMapper orderMapper;

  public void sendOrderCreated(OrderDto order) {
    sendMessage(new OrderCreatedEvent(orderMapper.toExternalDto(order)), String.valueOf(order.id()));
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
