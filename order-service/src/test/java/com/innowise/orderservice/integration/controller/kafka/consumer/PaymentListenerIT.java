package com.innowise.orderservice.integration.controller.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.innowise.common.model.dto.payment.PaymentDto;
import com.innowise.common.model.enums.PaymentStatus;
import com.innowise.common.model.event.PaymentCreatedEvent;
import com.innowise.common.model.event.PaymentStatusUpdatedEvent;
import com.innowise.orderservice.integration.AbstractIntegrationTest;
import com.innowise.orderservice.integration.annotation.IT;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.enums.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import net.jqwik.api.Arbitraries;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@IT
@RequiredArgsConstructor
class PaymentListenerIT extends AbstractIntegrationTest {

  private static final FixtureMonkey ORDERS_SUT = FixtureMonkey.builder()
      .plugin(new JqwikPlugin())
      .defaultNotNull(true)
      .register(Order.class, fm -> fm.giveMeBuilder(Order.class)
          .setNull("id")
          .size("orderItems", 0)
      )
      .build();

  private static final FixtureMonkey PAYMENTS_SUT = FixtureMonkey.builder()
      .plugin(new JqwikPlugin())
      .defaultNotNull(true)
      .nullableContainer(false)
      .nullableElement(false)
      .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
      .build();

  private final EntityManager em;

  private final KafkaTemplate<@NonNull String, @NonNull String> kafkaTemplate;
  private final TransactionTemplate tt;

  private final OrderService orderService;

  @Value("${spring.kafka.topics.payments.name}")
  private String paymentsTopic;

  @AfterEach
  void clearOrderTable() {
    tt.executeWithoutResult(_ -> {
      em.createQuery("DELETE FROM Order").executeUpdate();

    });
  }

  @Test
  void consumePaymentCreatedEvent_updateOrderStatusToProcessing() {
    var order = ORDERS_SUT.giveMeOne(Order.class);

    tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    tt.executeWithoutResult(_ -> em.persist(order));

    var payment = PAYMENTS_SUT.giveMeBuilder(PaymentDto.class)
        .set("orderId", order.getId())
        .set("status", PaymentStatus.PROCESSING)
        .sample();

    kafkaTemplate.send(MessageBuilder
        .withPayload(new PaymentCreatedEvent(payment))
        .setHeader(KafkaHeaders.TOPIC, paymentsTopic)
        .build()
    );

    await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(200))
        .untilAsserted(() -> {
          assertThat(em.find(Order.class, order.getId())).satisfies(foundOrder -> {
            assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
          });
        });
  }

  @Test
  void consumePaymentCreatedEvent_eventReceivedTwice_updateOrderStatusToProcessingOnlyOnce() {
    var order = ORDERS_SUT.giveMeBuilder(Order.class)
        .set("status", OrderStatus.NEW)
        .sample();

    tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    tt.executeWithoutResult(_ -> em.persist(order));

    var payment = PAYMENTS_SUT.giveMeBuilder(PaymentDto.class)
        .set("orderId", order.getId())
        .set("status", PaymentStatus.PROCESSING)
        .sample();

    var event = new PaymentCreatedEvent(payment);

    kafkaTemplate.send(MessageBuilder
        .withPayload(event)
        .setHeader(KafkaHeaders.TOPIC, paymentsTopic)
        .build()
    );
    kafkaTemplate.send(MessageBuilder
        .withPayload(event)
        .setHeader(KafkaHeaders.TOPIC, paymentsTopic)
        .build()
    );

    await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(200))
        .untilAsserted(() -> {
          assertThat(em.find(Order.class, order.getId())).satisfies(foundOrder -> {
            assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
          });
        });

    Mockito.verify(orderService, Mockito.times(1))
        .processPaymentCreation(Mockito.any());
  }

  @Test
  void consumePaymentStatusUpdatedEvent_paymentFailed_orderStatusStillProcessing() {
    var order = ORDERS_SUT.giveMeBuilder(Order.class)
        .set("status", OrderStatus.PROCESSING)
        .sample();

    tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    tt.executeWithoutResult(_ -> em.persist(order));

    kafkaTemplate.send(MessageBuilder
        .withPayload(new PaymentStatusUpdatedEvent(
            Arbitraries.strings().sample(),
            order.getId(),
            PaymentStatus.PROCESSING,
            PaymentStatus.FAILED
        ))
        .setHeader(KafkaHeaders.TOPIC, paymentsTopic)
        .build()
    );

    await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(200))
        .untilAsserted(() -> {
          assertThat(em.find(Order.class, order.getId())).satisfies(foundOrder -> {
            assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
          });
        });
  }

  @Test
  void consumePaymentStatusUpdatedEvent_paymentSucceeded_updateOrderStatusToDelivering() {
    var order = ORDERS_SUT.giveMeBuilder(Order.class)
        .set("status", OrderStatus.PROCESSING)
        .sample();

    tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    tt.executeWithoutResult(_ -> em.persist(order));

    kafkaTemplate.send(MessageBuilder
        .withPayload(new PaymentStatusUpdatedEvent(
            Arbitraries.strings().sample(),
            order.getId(),
            PaymentStatus.PROCESSING,
            PaymentStatus.SUCCEEDED
        ))
        .setHeader(KafkaHeaders.TOPIC, paymentsTopic)
        .build()
    );

    await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(200))
        .untilAsserted(() -> {
          assertThat(em.find(Order.class, order.getId())).satisfies(foundOrder -> {
            assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.DELIVERING);
          });
        });
  }

  @Test
  void consumePaymentStatusUpdatedEvent_paymentSucceededAndEventReceivedTwice_updateOrderStatusToDeliveringOnlyOnce() {
    var order = ORDERS_SUT.giveMeBuilder(Order.class)
        .set("status", OrderStatus.PROCESSING)
        .sample();

    tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    tt.executeWithoutResult(_ -> em.persist(order));

    var event = new PaymentStatusUpdatedEvent(
        Arbitraries.strings().sample(),
        order.getId(),
        PaymentStatus.PROCESSING,
        PaymentStatus.SUCCEEDED
    );

    kafkaTemplate.send(MessageBuilder
        .withPayload(event)
        .setHeader(KafkaHeaders.TOPIC, paymentsTopic)
        .build()
    );
    kafkaTemplate.send(MessageBuilder
        .withPayload(event)
        .setHeader(KafkaHeaders.TOPIC, paymentsTopic)
        .build()
    );

    await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(200))
        .untilAsserted(() -> {
          assertThat(em.find(Order.class, order.getId())).satisfies(foundOrder -> {
            assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.DELIVERING);
          });
        });

    Mockito.verify(orderService, Mockito.times(1))
        .processPaymentUpdate(Mockito.any(), Mockito.any());
  }

}
