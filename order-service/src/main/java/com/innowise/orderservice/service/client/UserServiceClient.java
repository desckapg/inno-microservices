package com.innowise.orderservice.service.client;

import com.innowise.orderservice.config.ApplicationConfig;
import com.innowise.orderservice.model.dto.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-service",
    configuration = ApplicationConfig.class)
public interface UserServiceClient {

  @GetMapping("/api/v1/users/{id}")
  UserDto findById(@PathVariable Long id);

}
