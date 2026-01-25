package com.innowise.orderservice.config;

import io.micrometer.tracing.exporter.SpanExportingPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

  @Bean
  SpanExportingPredicate noEureka() {
    return span -> !span.getTags().containsKey("http.url") || !span.getTags().get("http.url").contains("eureka");
  }

}
