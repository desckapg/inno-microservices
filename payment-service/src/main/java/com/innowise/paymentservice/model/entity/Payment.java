package com.innowise.paymentservice.model.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("payments")
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Payment {

  @Id
  private final String id;

  private final Long orderId;

  private final Long userId;

  private Status status;

  private final ZonedDateTime timestamp;

  @Field("payment_amount")
  private final BigDecimal amount;

  public enum Status {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED
  }

}
