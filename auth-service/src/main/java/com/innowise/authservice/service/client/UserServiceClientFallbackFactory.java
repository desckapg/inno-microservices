package com.innowise.authservice.service.client;

import com.innowise.common.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

  @Override
  public UserServiceClient create(Throwable cause) {
    log.error("Unexcepted error during request to user service", cause);
    throw new ExternalApiException("Internal server error");
  }

}
