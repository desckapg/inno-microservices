package com.innowise.paymentservice.integration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.innowise.common.test.extension.EagerWireMockExtension;
import java.util.List;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

public abstract class AbstractIntegrationTest {

  protected static final Faker FAKER = new Faker();

  @RegisterExtension
  protected static EagerWireMockExtension paymentSystemClientServer = EagerWireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  static MongoDBContainer mongo = new MongoDBContainer("mongo:latest");

  static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:latest");

  static {
    Startables.deepStart(mongo, kafka).join();
  }

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);

    registry.add("spring.kafka.bootstrap-servers", () -> List.of(kafka.getBootstrapServers()));

    registry.add("services.payment-system.url", paymentSystemClientServer::baseUrl);

  }

}
