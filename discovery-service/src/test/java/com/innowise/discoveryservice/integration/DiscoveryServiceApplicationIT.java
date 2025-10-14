package com.innowise.discoveryservice.integration;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DiscoveryServiceApplicationIT {

  @Test
  void contextLoads() {
    assertThatNoException();
  }

}
