package com.innowise.orderservice.integration;

import com.innowise.orderservice.integration.annotation.IT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@IT
class OrderServiceApplicationIT extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
    // Only check for application context loading
  }

}
