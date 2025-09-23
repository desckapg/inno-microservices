package com.innowise.userservice.model.dto.card;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.userservice.model.dto.DtoViews;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CardResponseDto(
    @JsonView({DtoViews.Basic.class}) Long id,
    @JsonView({DtoViews.Basic.class}) String number,
    @JsonView({DtoViews.Basic.class}) String holder,
    @JsonView({DtoViews.class}) LocalDate expirationDate,
    @JsonView({DtoViews.class}) Long userId
) {

}
