package com.innowise.userservice.model.mapper;

import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.entity.Card;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CardMapper {

  @Mapping(target = "userId", expression = "java(card.getUser().getId())")
  CardDto toDto(Card card);

  @Mapping(target = "user", ignore = true)
  Card toEntity(CardDto dto);

}
