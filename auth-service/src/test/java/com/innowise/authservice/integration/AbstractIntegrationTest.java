package com.innowise.authservice.integration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.innowise.common.test.extension.EagerWireMockExtension;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({SystemStubsExtension.class})
public abstract class AbstractIntegrationTest {

  protected static final Faker FAKER = new Faker();

  private static final String TEST_ACCESS_KEY = "6e4d3f160f46d03de0d5f3ac52d2e19797cbbe9ff71ab1c168ce31bb5d4df87e";
  private static final String TEST_REFRESH_KEY = "b1819099b124e415850b817aba0b2b42ade9ac3167cc2b8d85aca419d728cf3a";

  private static final int TEST_ACCESS_EXPIRATION = 900;
  private static final int TEST_REFRESH_EXPIRATION = 604800;


  @RegisterExtension
  protected static EagerWireMockExtension userServiceClientServer = EagerWireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:18.1-alpine");

  static {
    Startables.deepStart(postgres).join();
    userServiceClientServer.startServerIfRequired();
  }

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    registry.add("services.user-service.url", userServiceClientServer::baseUrl);
  }

  @SystemStub
  private final EnvironmentVariables envVariables = new EnvironmentVariables();

  @BeforeEach
  void prepareTokenService() {
    envVariables.set("JWT_ACCESS_KEY", TEST_ACCESS_KEY);
    envVariables.set("JWT_REFRESH_KEY", TEST_REFRESH_KEY);
    envVariables.set("JWT_EXPIRATION", String.valueOf(TEST_ACCESS_EXPIRATION));
    envVariables.set("JWT_REFRESH_EXPIRATION", String.valueOf(TEST_REFRESH_EXPIRATION));
  }

}
