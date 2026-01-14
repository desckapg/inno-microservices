package com.innowise.orderservice.integration;

import com.redis.testcontainers.RedisContainer;
import java.util.List;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public abstract class AbstractIntegrationTest {

  protected static final Faker FAKER = new Faker();

  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:18.1-alpine");

  static KafkaContainer kafka = new KafkaContainer("apache/kafka:latest");

  static RedisContainer redis = new RedisContainer("redis:8.4-alpine");

  static {
    Startables.deepStart(postgres, kafka, redis).join();
  }

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    registry.add("spring.kafka.bootstrap-servers", () -> List.of(kafka.getBootstrapServers()));

    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getRedisPort);
    registry.add("spring.data.redis.username", () -> "");
    registry.add("spring.data.redis.password", () -> "");

  }

}
