package com.innowise.orderservice.config;

import feign.FeignException;
import feign.Logger;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  public Customizer<Resilience4JCircuitBreakerFactory> defaultCircuirBreakerFactoryCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
        .circuitBreakerConfig(CircuitBreakerConfig.custom()
            .recordException(thr -> {
              if (thr instanceof FeignException feignEx) {
                return feignEx.status() / 100 != 2 && feignEx.status() / 100 != 4;
              }
              return true;
            })
            .build()
        )
        .build()
    );
  }

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }

}
