package com.innowise.gateway.service.client;

import com.innowise.common.exception.ExternalApiException;
import com.innowise.common.model.dto.ErrorDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@CircuitBreaker(name = "auth-service")
@TimeLimiter(name = "auth-service")
@Retry(name = "auth-service")
public class AuthServiceClient {

  private final WebClient webClient;

  public AuthServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
    this.webClient = loadBalancedWebClientBuilder
        .baseUrl("lb://auth-service")
        .build();
  }

  public Mono<Void> validate(String accessToken) {
    return webClient
        .get()
        .uri("/api/v1/auth/validate")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            response -> response.bodyToMono(ErrorDto.class)
                .flatMap(errorDto -> Mono.error(new ExternalApiException(errorDto)))
        )
        .bodyToMono(Void.class)
        .doOnError(thr -> {
          log.warn("Exception during validating token {}", accessToken);
          log.warn(thr.getMessage(), thr);
        });
  }

}
