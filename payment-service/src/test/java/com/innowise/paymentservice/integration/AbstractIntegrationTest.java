package com.innowise.paymentservice.integration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.innowise.common.test.extension.EagerWireMockExtension;
import com.redis.testcontainers.RedisContainer;
import java.util.List;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

public abstract class AbstractIntegrationTest {

  @RegisterExtension
  protected static EagerWireMockExtension stripeClientServer = EagerWireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  static MongoDBContainer mongo = new MongoDBContainer("mongo:latest");

  static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:latest");

  static RedisContainer redis = new RedisContainer("redis:latest");

  static {
    Startables.deepStart(mongo, kafka, redis).join();
  }

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);

    registry.add("spring.kafka.bootstrap-servers", () -> List.of(kafka.getBootstrapServers()));

    registry.add("services.stipe.url", stripeClientServer::baseUrl);


    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getRedisPort);
    registry.add("spring.data.redis.username", () -> "");
    registry.add("spring.data.redis.password", () -> "");

  }

}
