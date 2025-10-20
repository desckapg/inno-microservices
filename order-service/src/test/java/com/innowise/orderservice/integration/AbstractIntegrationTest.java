package com.innowise.orderservice.integration;

import static uk.org.webcompere.systemstubs.resource.PropertySource.fromResource;

import net.bytebuddy.asm.MemberSubstitution.Substitution.Chain.Step.ForDelegation.OffsetMapping.Factory;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public abstract class AbstractIntegrationTest {

  protected static final Faker FAKER = new Faker();

  @SystemStub
  protected static EnvironmentVariables envVars = new EnvironmentVariables()
      .set(fromResource(".env.test"));

  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:latest");

  static {
    Startables.deepStart(postgres).join();
  }

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

}
