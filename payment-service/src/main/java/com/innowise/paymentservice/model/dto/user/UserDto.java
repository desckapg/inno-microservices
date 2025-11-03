package com.innowise.paymentservice.model.dto.user;

import com.innowise.paymentservice.model.dto.card.CardDto;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record UserDto(

    Long id,
    String name,
    String surname,
    LocalDate birthDate,
    String email,
    List<CardDto> cards

) implements Serializable {

}
