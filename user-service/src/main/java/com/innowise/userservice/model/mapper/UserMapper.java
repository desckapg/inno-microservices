package com.innowise.userservice.model.mapper;

import com.innowise.userservice.model.dto.user.UserCreateRequestDto;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.dto.user.UserWithCardsDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {CardMapper.class}
)
public interface UserMapper {

  @Mapping(target = "cards", ignore = true)
  User toEntity(UserCreateRequestDto dto);

  UserDto toDto(User user);

  UserWithCardsDto toWithCardsDto(User user);

  @AfterMapping
  default void linkCards(@MappingTarget User user) {
    user.getCards().forEach(card -> card.setUser(user));
  }
}
