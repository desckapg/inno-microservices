package com.innowise.userservice.model.mapper;

import com.innowise.userservice.model.dto.user.UserCreateRequestDto;
import com.innowise.userservice.model.dto.user.UserResponseDto;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = CardMapper.class
)
public interface UserMapper {

  @Mapping(target = "cards", ignore = true)
  User toEntity(UserCreateRequestDto dto);

  @Mapping(target = "cards", expression = "java(List.of())")
  UserResponseDto toDto(User user);

  UserResponseDto toWithCardsDto(User user);

}
