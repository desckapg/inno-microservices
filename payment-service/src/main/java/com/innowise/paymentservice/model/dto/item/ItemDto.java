package com.innowise.paymentservice.model.dto.item;

import java.io.Serializable;
import java.math.BigDecimal;

public record ItemDto(

    Long id,
    String name,
    BigDecimal price

) implements Serializable {

}
