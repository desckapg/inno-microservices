package com.innowise.common.model.event;

import com.innowise.common.model.enums.PaymentStatus;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentStatusUpdatedEvent extends BaseEvent implements Serializable {

  @Serial
  private static final long serialVersionUID = -6228686413551665018L;

  private final String id;
  private final Long orderId;
  private final PaymentStatus previousStatus;
  private final PaymentStatus newStatus;

}
