package com.innowise.orderservice.integration;

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
      new PostgreSQLContainer<>("postgres:latest");

  static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:latest");

  static {
    Startables.deepStart(postgres, kafka).join();
  }

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    registry.add("spring.kafka.bootstrap-servers", () -> List.of(kafka.getBootstrapServers()));
  }

}
