package com.innowise.authservice.model.dto.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.innowise.authservice.validation.groups.OnRefresh;
import com.innowise.authservice.validation.groups.OnValidate;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record TokenDto(

    @NotBlank(
        message = "Access token must be provided",
        groups = OnValidate.class
    )
    String accessToken,

    @NotBlank(
        message = "Refresh token must be provided",
        groups = OnRefresh.class
    )
    String refreshToken
) {

}
