package com.innowise.userservice.model.dto.card;

import java.io.Serializable;
import java.time.LocalDate;

public record CardResponseDto(
    Long id,
    String number,
    String holder,
    LocalDate expirationDate,
    Long userId
) implements Serializable {

}
