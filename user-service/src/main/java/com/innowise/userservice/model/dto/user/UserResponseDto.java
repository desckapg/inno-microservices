package com.innowise.userservice.model.dto.user;

import com.innowise.userservice.model.dto.card.CardResponseDto;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record UserResponseDto(
    Long id,
    String name,
    String surname,
    LocalDate birthDate,
    String email,
    List<CardResponseDto> cards
) implements Serializable {

}
