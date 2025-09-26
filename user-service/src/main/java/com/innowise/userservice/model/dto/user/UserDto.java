package com.innowise.userservice.model.dto.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.time.LocalDate;

@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public record UserDto(
    Long id,
    String name,
    String surname,
    LocalDate birthDate,
    String email
) {

}
