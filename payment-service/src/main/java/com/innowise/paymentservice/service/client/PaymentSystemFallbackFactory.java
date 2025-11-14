package com.innowise.paymentservice.service.client;

import com.innowise.common.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentSystemFallbackFactory implements FallbackFactory<PaymentSystemClient> {

  @Override
  public PaymentSystemClient create(Throwable cause) {
    log.error(cause.getMessage(), cause);
    throw new ExternalApiException("Exception during request to payment system");
  }

}
