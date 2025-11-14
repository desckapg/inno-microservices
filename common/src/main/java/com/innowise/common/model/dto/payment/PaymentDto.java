package com.innowise.common.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.innowise.common.model.enums.PaymentStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentDto(

    String id,
    Long orderId,
    Long userId,
    PaymentStatus status,
    Instant timestamp,
    BigDecimal amount

) implements Serializable {

}
