package com.innowise.authservice.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }

}
