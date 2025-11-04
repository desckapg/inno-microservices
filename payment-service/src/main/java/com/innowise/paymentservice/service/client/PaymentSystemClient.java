package com.innowise.paymentservice.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    name = "payment-system",
    url = "${services.payment-system.url}",
    fallback = PaymentSystemFallbackFactory.class
)
public interface PaymentSystemClient {

  @GetMapping(
      value = "/api/v1.0/random",
      params = {
          "min=" + Integer.MIN_VALUE,
          "max=" + Integer.MAX_VALUE
      }
  )
  Integer processPayment();

}
