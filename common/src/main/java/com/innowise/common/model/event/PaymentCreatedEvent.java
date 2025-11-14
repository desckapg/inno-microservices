package com.innowise.common.model.event;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.innowise.common.model.dto.payment.PaymentDto;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class PaymentCreatedEvent implements Serializable {

  @Serial
  private static final long serialVersionUID = -1780395708349808221L;

  @JsonUnwrapped
  private final PaymentDto payment;

  public PaymentCreatedEvent(PaymentDto payment) {
    this.payment = payment;
  }
}
