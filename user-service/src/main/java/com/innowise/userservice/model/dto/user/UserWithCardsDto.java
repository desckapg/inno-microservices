package com.innowise.userservice.model.dto.user;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.innowise.userservice.model.dto.card.CardDto;
import java.time.LocalDate;
import java.util.List;

@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public record UserWithCardsDto(
    Long id,
    String name,
    String surname,
    LocalDate birthDate,
    String email,
    List<CardDto> cards) {

}