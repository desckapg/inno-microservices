package com.innowise.orderservice.config;

import com.innowise.auth.security.provider.AuthTokenProviderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

  private final AuthTokenProviderInterceptor authTokenProviderInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authTokenProviderInterceptor);
  }
}
