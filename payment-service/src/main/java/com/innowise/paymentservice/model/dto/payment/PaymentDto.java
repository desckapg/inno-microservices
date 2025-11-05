package com.innowise.paymentservice.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentDto(

    String id,
    Long orderId,
    Long userId,
    Status status,
    ZonedDateTime timestamp,
    BigDecimal amount

) implements Serializable {

  public enum Status {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED
  }

}
