package com.innowise.paymentservice.service.client;


import com.innowise.common.exception.ExternalApiException;
import com.innowise.common.model.enums.PaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@CircuitBreaker(name = "stripe-client", fallbackMethod = "processPaymentFallback")
@TimeLimiter(name = "stripe-client")
@Retry(name = "stripe-client")
@Slf4j
@Component
public class StripeClient {

  private final WebClient webClient;

  public StripeClient(WebClient.Builder webClientBuilder,
      @Value("${services.stipe.url}") String stipeUrl) {
    this.webClient = webClientBuilder.baseUrl(stipeUrl).build();
  }

  public Mono<PaymentStatus> processPayment() {
    return webClient
        .get()
        .uri("api/v1.0/random", Map.of("min", Integer.MIN_VALUE, "max", Integer.MAX_VALUE))
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            ClientResponse::createError
        )
        .bodyToMono(Integer[].class)
        .<PaymentStatus>handle((arr, sink) -> {
          if (arr == null || arr.length == 0 || arr[0] == null) {
            sink.error(new ExternalApiException("Illegal received array value from stripe"));
            return;
          }
          var payment = arr[0] % 2 == 0 ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;
          sink.next(payment);
        })
        .onErrorMap(thr -> {
          log.error(thr.getMessage(), thr.getCause());
          return new ExternalApiException("Exception during handle response from stripe");
        });
  }

  private PaymentStatus processPaymentFallback() {
    throw new ExternalApiException("Skipping request due to the circuit breaker opened");
  }

}
