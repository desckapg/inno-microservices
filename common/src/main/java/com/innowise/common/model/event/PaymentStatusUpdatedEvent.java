package com.innowise.common.model.event;

import com.innowise.common.model.enums.PaymentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentStatusUpdatedEvent extends BaseEvent implements Serializable {

  @Serial
  private static final long serialVersionUID = -6228686413551665018L;

  @NotBlank(
      message = "Id must be provided"
  )
  private final String id;

  @NotNull(
      message = "Order id must be provided"
  )
  @Min(
      value = 1,
      message = "Order id must be not less than 1"
  )
  private final Long orderId;

  @NotNull(
      message = "Previous payment status must be provided"
  )
  private final PaymentStatus previousStatus;

  @NotNull(
      message = "New payment status must be provided"
  )
  private final PaymentStatus newStatus;

}
