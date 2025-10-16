package com.innowise.auth.model.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record TokenDto(
    String accessToken,
    String refreshToken
) {

}
