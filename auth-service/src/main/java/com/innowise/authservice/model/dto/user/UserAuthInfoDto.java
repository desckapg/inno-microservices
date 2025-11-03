package com.innowise.authservice.model.dto.user;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.authservice.model.dto.UserConstraints;
import jakarta.validation.Valid;
import lombok.Builder;

@Builder
@JsonView(UserConstraints.Register.class)
public record UserAuthInfoDto(

    @JsonUnwrapped
    @Valid
    UserAuthDto authDto,

    @JsonUnwrapped
    UserInfoDto infoDto

) {

}
