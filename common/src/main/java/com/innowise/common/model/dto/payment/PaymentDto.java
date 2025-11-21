package com.innowise.common.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.innowise.common.model.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentDto(

    @NotBlank(
        message = "Id is required"
    )
    String id,

    @NotNull(
        message = "Order id is required"
    )
    @Min(
        value = 1,
        message = "Order id must be not less than 1"
    )
    Long orderId,

    @NotNull(
        message = "User id is required"
    )
    @Min(
        value = 1,
        message = "User id must be not less than 1"
    )
    Long userId,

    @NotNull(
        message = "Payment status is required"
    )
    PaymentStatus status,

    @NotNull(
        message = "Timestamp is required"
    )
    Instant timestamp,

    @NotNull(
        message = "Amount is required"
    )
    @DecimalMin(
        value = "0.0",
        message = "Amount must be greater than 0",
        inclusive = false

    )
    BigDecimal amount

) implements Serializable {

}
