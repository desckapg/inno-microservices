package com.innowise.common.model.event;

import com.innowise.common.model.enums.PaymentStatus;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class PaymentStatusUpdatedEvent implements Serializable {

  @Serial
  private static final long serialVersionUID = -6228686413551665018L;

  private final String id;
  private final PaymentStatus previousStatus;
  private final PaymentStatus newStatus;

  public PaymentStatusUpdatedEvent(String id, PaymentStatus previousStatus, PaymentStatus newStatus) {
    this.id = id;
    this.previousStatus = previousStatus;
    this.newStatus = newStatus;
  }
}
