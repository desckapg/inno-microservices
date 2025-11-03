package com.innowise.paymentservice.model.dto.card;

import java.io.Serializable;
import java.time.LocalDate;

public record CardDto(

    Long id,
    String number,
    String holder,
    LocalDate expirationDate,
    Long userId

) implements Serializable {

}
