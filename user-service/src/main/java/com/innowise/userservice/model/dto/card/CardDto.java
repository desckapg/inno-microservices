package com.innowise.userservice.model.dto.card;

import java.time.LocalDate;

public record CardDto(
    Long id,
    String number,
    String holder,
    LocalDate expirationDate,
    Long userId
) {

}
