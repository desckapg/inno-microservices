package com.innowise.userservice.integration;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

public abstract class AbstractIntegrationTest {

  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:latest");

  static RedisContainer redis =
      new RedisContainer("redis:latest");

  static {
    Startables.deepStart(postgres, redis).join();
  }

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getRedisPort);
  }

}
