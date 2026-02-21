package com.innowise.gateway.integration;

import com.innowise.gateway.integration.annotation.IT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

@IT
class GatewayApplicationIT extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
    assertThatNoException();
  }

}
