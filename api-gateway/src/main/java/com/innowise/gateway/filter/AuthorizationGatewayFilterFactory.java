package com.innowise.gateway.filter;

import com.innowise.common.exception.ExternalApiException;
import com.innowise.gateway.service.client.AuthServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
public class AuthorizationGatewayFilterFactory extends
    AbstractGatewayFilterFactory<AuthorizationGatewayFilterFactory.Config> {

  private final JsonMapper jsonMapper;
  private final AuthServiceClient authServiceClient;

  public AuthorizationGatewayFilterFactory(AuthServiceClient authServiceClient,
      JsonMapper jsonMapper) {
    super(Config.class);
    this.authServiceClient = authServiceClient;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      var request = exchange.getRequest();
      var response = exchange.getResponse();
      var tokenHeaderVal = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (tokenHeaderVal == null || !tokenHeaderVal.startsWith("Bearer ")) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return Mono.empty();
      }
      var token = tokenHeaderVal.substring(7);
      return authServiceClient.validate(token)
          .then(chain.filter(exchange))
          .onErrorResume(thr -> {
            if (thr instanceof ExternalApiException externalApiException) {
              response.setStatusCode(HttpStatus.UNAUTHORIZED);
              if (externalApiException.getErrorDto() != null) {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return response.writeWith(
                    Mono.just(response.bufferFactory()
                        .wrap(jsonMapper.writeValueAsBytes(externalApiException.getErrorDto()))
                    )
                );
              }
              return response.setComplete();
            } else {
              log.error(thr.getMessage(), thr);
              response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return response.setComplete();
          });
    };
  }

  public static class Config {
  }
}
