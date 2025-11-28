package com.innowise.paymentservice.integration.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.innowise.common.model.dto.order.OrderDto;
import com.innowise.common.model.enums.PaymentStatus;
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
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import wiremock.org.eclipse.jetty.http.HttpHeader;
import wiremock.org.eclipse.jetty.http.HttpStatus;
import wiremock.org.eclipse.jetty.http.MimeTypes.Type;

@IT
@RequiredArgsConstructor
class PaymentServiceIT extends AbstractIntegrationTest {

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

  private final MongoTemplate mongoTemplate;

  private final PaymentService paymentService;
  private final PaymentRepository paymentRepository;
  private final PaymentProducer paymentProducer;

  @Test
  void create() {
    var orderDto = SUT.giveMeOne(OrderDto.class);

    var paymentDto = paymentService.create(orderDto);

    verify(paymentRepository).save(any(Payment.class));
    verify(paymentProducer).sendPaymentCreated(paymentDto);

    assertThat(paymentDto.id()).isNotNull();
    assertThat(paymentDto.userId()).isEqualTo(orderDto.user().id());
    assertThat(paymentDto.orderId()).isEqualTo(orderDto.id());

    assertThat(
        mongoTemplate.findOne(query(where("_id").in(paymentDto.id())), Payment.class)).isNotNull();
  }

  @Test
  void processPayment_paymentSuccessful_updateStatusToSucceeded() {
    var payment = mongoTemplate.save(SUT.giveMeOne(Payment.class));

    stripeClientServer.stubFor(
        get(urlPathTemplate("**"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK_200)
                .withHeader(HttpHeader.CONTENT_TYPE.asString(), Type.APPLICATION_JSON.asString())
                .withBody("[2]")
            )
    );

    paymentService.processPayment(payment.getId());

    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(() -> {
          assertThat(mongoTemplate.findOne(query(where("_id").in(payment.getId())), Payment.class))
              .satisfies(
                  p -> assertThat(p).isNotNull(),
                  p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED)
              );
        });


  }

  @Test
  void processPayment_paymentFailed_updateStatusToFailed() {
    var payment = mongoTemplate.save(SUT.giveMeOne(Payment.class));

    stripeClientServer.stubFor(
        get(urlPathTemplate("**"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK_200)
                .withHeader(HttpHeader.CONTENT_TYPE.asString(), Type.APPLICATION_JSON.asString())
                .withBody("[1]")
            )
    );

    paymentService.processPayment(payment.getId());

    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(() -> {
          assertThat(mongoTemplate.findOne(query(where("_id").in(payment.getId())), Payment.class))
              .satisfies(
                  p -> assertThat(p).isNotNull(),
                  p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED)
              );
        });
  }

  @Test
  void processPayment_exceptionDuringRequestToPaymentSystem_updateStatusToFailed() {
    var payment = mongoTemplate.save(SUT.giveMeOne(Payment.class));

    stripeClientServer.stubFor(
        get(urlPathTemplate("**"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
            )
    );

    paymentService.processPayment(payment.getId());

    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(() -> {
          assertThat(mongoTemplate.findOne(query(where("_id").in(payment.getId())), Payment.class))
              .satisfies(
                  p -> assertThat(p).isNotNull(),
                  p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED)
              );
        });
  }

}
