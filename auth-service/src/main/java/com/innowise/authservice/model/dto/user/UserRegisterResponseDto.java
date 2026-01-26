package com.innowise.authservice.model.dto.user;

import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;

@Builder
public record UserRegisterResponseDto(
    Long id,
    String login,
    String name,
    String surname,
    LocalDate birthDate,
    String email,
    Set<String> roles
) {

}
