package com.innowise.paymentservice.model.entity;

import com.innowise.common.model.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

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

  private PaymentStatus status;

  @Field(targetType = FieldType.TIMESTAMP)
  private final Instant timestamp;

  @Field(value = "payment_amount", targetType = FieldType.STRING)
  private final BigDecimal amount;

}
