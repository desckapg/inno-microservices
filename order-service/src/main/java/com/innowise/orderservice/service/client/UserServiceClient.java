package com.innowise.orderservice.service.client;

import com.innowise.auth.model.AuthConstants;
import com.innowise.orderservice.model.dto.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "user-service",
    fallbackFactory = UserSeviceClientFallbackFactory.class
)
public interface UserServiceClient {

  @GetMapping("/api/v1/users/{id}")
  UserDto findById(
      @PathVariable Long id,
      @RequestHeader(AuthConstants.AUTH_HEADER) String authorization
  );

}
