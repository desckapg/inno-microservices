package com.innowise.authservice.model.dto.user;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.authservice.model.dto.UserConstraints;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Builder
@JsonView(UserConstraints.BaseAuth.class)
public record UserAuthDto(

    @JsonView(UserConstraints.FindAuth.class)
    Long id,

    @JsonView(UserConstraints.BaseAuth.class)
    @JsonUnwrapped
    @Valid
    CredentialDto credentials,

    @JsonView(UserConstraints.FindAuth.class)
    Set<String> roles,

    @JsonView(UserConstraints.FindAuth.class)
    Long userId

) {

}
