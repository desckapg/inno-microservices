package com.innowise.authservice.model.mapper;

import com.innowise.authservice.model.dto.user.UserAuthDto;
import com.innowise.authservice.model.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    componentModel = "spring"
)
public interface UserMapper {

  UserAuthDto toDto(User user);

}
