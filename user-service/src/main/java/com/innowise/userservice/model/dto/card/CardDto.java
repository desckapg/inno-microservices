package com.innowise.userservice.model.dto.card;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.time.LocalDate;

@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public record CardDto(
    Long id,
    String number,
    String holder,
    LocalDate expirationDate,
    Long userId
) {

}
