package com.innowise.gateway.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.lifecycle.Startables;

public abstract class AbstractIntegrationTest {

  protected static KeycloakContainer keycloak = new KeycloakContainer(
      "quay.io/keycloak/keycloak:26.5.3");

  static {
    Startables.deepStart(keycloak);
  }

  @DynamicPropertySource
  static void registerKeycloakProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/master");
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> keycloak.getAuthServerUrl() + "/realms/master/protocol/openid-connect/certs");
  }

}
