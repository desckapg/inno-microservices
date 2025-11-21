package com.innowise.paymentservice.integration.controller.kafka.listener;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.innowise.common.exception.ExternalApiException;
import com.innowise.common.model.dto.order.OrderDto;
import com.innowise.common.model.enums.PaymentStatus;
import com.innowise.common.model.event.OrderCreatedEvent;
import com.innowise.paymentservice.controller.kafka.consumer.OrderListener;
import com.innowise.paymentservice.integration.AbstractIntegrationTest;
import com.innowise.paymentservice.integration.annotation.IT;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.service.PaymentService;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import wiremock.org.eclipse.jetty.http.HttpHeader;
import wiremock.org.eclipse.jetty.http.HttpStatus;
import wiremock.org.eclipse.jetty.http.MimeTypes.Type;

@IT
@RequiredArgsConstructor
class OrderListenerIT extends AbstractIntegrationTest {

  private static final FixtureMonkey SUT = FixtureMonkey.builder()
      .plugin(new JqwikPlugin())
      .plugin(new JakartaValidationPlugin())
      .objectIntrospector(new ConstructorPropertiesArbitraryIntrospector())
      .defaultNotNull(true)
      .nullableContainer(false)
      .nullableElement(false)
      .register(Payment.class, fm -> fm.giveMeBuilder(Payment.class)
          .setNull("id")
          .set("status", PaymentStatus.PENDING)
      )
      .build();

  private final KafkaTemplate<@NonNull String, @NonNull String> kafkaTemplate;

  private final MongoTemplate mongoTemplate;

  private final PaymentService paymentService;
  private final OrderListener orderListener;

  @Value("${spring.kafka.topics.orders.name}")
  private String orderTopic;

  @Test
  void consumeOrderCreatedEvent() {
    var orderDto = SUT.giveMeOne(OrderDto.class);

    kafkaTemplate.send(MessageBuilder
        .withPayload(new OrderCreatedEvent(orderDto))
        .setHeader(KafkaHeaders.TOPIC, orderTopic)
        .build()
    );

    stripeClientServer.stubFor(
        get(urlPathTemplate("**"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK_200)
                .withHeader(HttpHeader.CONTENT_TYPE.asString(), Type.APPLICATION_JSON.asString())
                .withBody("[2]")
            )
    );

    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(200))
        .untilAsserted(() -> {
          var payment = mongoTemplate.findOne(query(where("orderId").is(orderDto.id())),
              Payment.class);

          assertThat(payment).isNotNull();
          assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        });

    Mockito.verify(paymentService, Mockito.times(1)).create(Mockito.any());
  }

  @Test
  void consumeOrderCreatedEvent_eventReceivedTwice_processedOnlyOne() {
    var orderDto = SUT.giveMeOne(OrderDto.class);

    var orderCreatedEvent = new OrderCreatedEvent(orderDto);

    kafkaTemplate.send(MessageBuilder
        .withPayload(orderCreatedEvent)
        .setHeader(KafkaHeaders.TOPIC, orderTopic)
        .build()
    );
    kafkaTemplate.send(MessageBuilder
        .withPayload(orderCreatedEvent)
        .setHeader(KafkaHeaders.TOPIC, orderTopic)
        .build()
    );

    stripeClientServer.stubFor(
        get(urlPathTemplate("**"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK_200)
                .withHeader(HttpHeader.CONTENT_TYPE.asString(), Type.APPLICATION_JSON.asString())
                .withBody("[2]")
            )
    );

    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(200))
        .untilAsserted(() -> {
          var payment = mongoTemplate.findOne(query(where("orderId").is(orderDto.id())),
              Payment.class);

          assertThat(payment).isNotNull();
          assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        });

    Mockito.verify(paymentService, Mockito.times(1)).create(Mockito.any());
    Mockito.verify(paymentService, Mockito.times(1)).processPayment(Mockito.any());
  }

  @Test
  void consumeOrderCreatedEvent_invalidOrderDto_shouldSendToDLT() {
    var invalidOrderDto = new OrderDto(
        null,
        null,
        null,
        null
    );

    var orderCreatedEvent = new OrderCreatedEvent(invalidOrderDto);

    kafkaTemplate.send(MessageBuilder
        .withPayload(orderCreatedEvent)
        .setHeader(KafkaHeaders.TOPIC, orderTopic)
        .build()
    );

    await().during(Duration.ofSeconds(5)).until(() -> true);

    Mockito.verify(orderListener, Mockito.never())
        .consumeOrderCreatedEvent(Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void consumeOrderCreatedEvent_exceptionFromService_shouldRetrySeveralTimes() {
    var orderDto = SUT.giveMeOne(OrderDto.class);

    var orderCreatedEvent = new OrderCreatedEvent(orderDto);

    Mockito.doThrow(ExternalApiException.class)
        .when(paymentService)
        .create(Mockito.any());

    kafkaTemplate.send(MessageBuilder
        .withPayload(orderCreatedEvent)
        .setHeader(KafkaHeaders.TOPIC, orderTopic)
        .build()
    );

    await()
        .atMost(Duration.ofSeconds(10))
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(() -> {
          Mockito.verify(paymentService, Mockito.atLeast(2))
              .create(Mockito.any());
        });
  }

}
