package com.innowise.paymentservice.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  public Logger.Level loggerLevel() {
    return Logger.Level.BASIC;
  }

}
