package com.innowise.userservice.model.dto.user;

import com.innowise.userservice.model.dto.card.CardDto;
import java.time.LocalDate;
import java.util.List;

public record UserDto(
    Long id,
    String name,
    String surname,
    LocalDate birthDate,
    String email,
    List<CardDto> cards
) {

}
