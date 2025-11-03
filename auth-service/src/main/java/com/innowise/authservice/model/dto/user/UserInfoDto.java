package com.innowise.authservice.model.dto.user;

import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.authservice.model.dto.UserConstraints;
import java.time.LocalDate;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;

@NullMarked
@JsonView(UserConstraints.BaseInfo.class)
@Builder
public record UserInfoDto(

    @JsonView(UserConstraints.FindInfo.class)
    Long id,

    @JsonView(UserConstraints.BaseInfo.class)
    String name,

    @JsonView(UserConstraints.BaseInfo.class)
    String surname,

    @JsonView(UserConstraints.BaseInfo.class)
    LocalDate birthDate,

    @JsonView(UserConstraints.BaseInfo.class)
    String email

) {

}
