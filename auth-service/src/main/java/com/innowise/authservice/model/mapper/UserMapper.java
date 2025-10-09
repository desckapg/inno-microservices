package com.innowise.authservice.model.mapper;

import com.innowise.authservice.model.dto.user.UserDto;
import com.innowise.authservice.model.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    componentModel = "spring"
)
public interface UserMapper {

  @Mapping(target = "login", source = "credentials.login")
  UserDto toDto(User user);

}
