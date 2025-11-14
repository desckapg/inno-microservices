package com.innowise.paymentservice.integration.controller.kafka.listener;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.innowise.common.model.dto.order.OrderDto;
import com.innowise.common.model.enums.PaymentStatus;
import com.innowise.common.model.event.OrderCreatedEvent;
import com.innowise.paymentservice.controller.kafka.producer.PaymentProducer;
import com.innowise.paymentservice.integration.AbstractIntegrationTest;
import com.innowise.paymentservice.integration.annotation.IT;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import java.math.BigDecimal;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import net.jqwik.api.Arbitraries;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
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
      .objectIntrospector(new ConstructorPropertiesArbitraryIntrospector())
      .defaultNotNull(true)
      .nullableContainer(false)
      .nullableElement(false)
      .register(OrderDto.class, fm -> fm.giveMeBuilder(OrderDto.class)
          .set("id", Arbitraries.longs().greaterOrEqual(1L))
          .set("user.id", Arbitraries.longs().greaterOrEqual(1L))
      )
      .register(Payment.class, fm -> fm.giveMeBuilder(Payment.class)
          .setNull("id")
          .set("userId", Arbitraries.longs().greaterOrEqual(1L))
          .set("orderId", Arbitraries.longs().greaterOrEqual(1L))
          .set("amount", Arbitraries.bigDecimals().greaterThan(BigDecimal.ZERO))
          .set("status", PaymentStatus.PENDING)
      )
      .build();

  private final KafkaTemplate<@NonNull String, @NonNull String> kafkaTemplate;
  private final MongoTemplate mongoTemplate;

  private final PaymentService paymentService;
  private final PaymentRepository paymentRepository;
  private final PaymentProducer paymentProducer;

  @Test
  void consumeOrderCreatedEvent() {
    var orderDto = SUT.giveMeOne(OrderDto.class);

    kafkaTemplate.send(MessageBuilder
        .withPayload(new OrderCreatedEvent(orderDto))
        .setHeader(KafkaHeaders.TOPIC, "queuing.order_service.orders")
        .build()
    );

    paymentSystemClientServer.stubFor(
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
          var payment = mongoTemplate.findOne(query(where("orderId").is(orderDto.id())), Payment.class);

          assertThat(payment).isNotNull();
          assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        });

  }

}
