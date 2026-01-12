package com.innowise.authservice.service.client;

import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.authservice.model.dto.UserConstraints;
import com.innowise.authservice.model.dto.user.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
    name = "user-service",
    url = "${services.user-service.url:lb://user-service}",
    fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {

  @PostMapping("/api/v1/users")
  @JsonView(UserConstraints.FindInfo.class)
  UserInfoDto create(UserInfoDto userInfoDto);

  @DeleteMapping("/api/v1/users/{id}")
  void delete(@PathVariable Long id);

}
