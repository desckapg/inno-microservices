package com.innowise.auth.model.credential;

import lombok.Builder;

@Builder
public record CredentialDto(
    String login,
    String password
) {

}
