package com.innowise.userservice.model.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.userservice.model.dto.DtoViews;
import com.innowise.userservice.model.dto.card.CardResponseDto;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDto(
    @JsonView({DtoViews.Basic.class}) Long id,
    @JsonView({DtoViews.Basic.class}) String name,
    @JsonView({DtoViews.Basic.class}) String surname,
    @JsonView({DtoViews.Basic.class}) LocalDate birthDate,
    @JsonView({DtoViews.Basic.class}) String email,
    @JsonView({DtoViews.WithCards.class}) List<CardResponseDto> cards
) {

}
