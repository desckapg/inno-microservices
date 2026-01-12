package com.innowise.gateway.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

import com.innowise.gateway.integration.annotation.IT;
import org.junit.jupiter.api.Test;

@IT
class GatewayApplicationIT {

  @Test
  void contextLoads() {
    assertThatNoException();
  }

}
