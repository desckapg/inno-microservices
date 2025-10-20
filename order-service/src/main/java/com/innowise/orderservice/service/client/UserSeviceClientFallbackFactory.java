package com.innowise.orderservice.service.client;

import com.innowise.common.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserSeviceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

  @Override
  public UserServiceClient create(Throwable cause) {
    throw new ExternalApiException("Exception during request to User Service");
  }
}
