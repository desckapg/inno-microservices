package com.innowise.userservice.model.mapper;

import com.innowise.userservice.model.dto.card.CardCreateRequestDto;
import com.innowise.userservice.model.dto.card.CardResponseDto;
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
  CardResponseDto toDto(Card card);

  @Mapping(target = "user", ignore = true)
  Card toEntity(CardCreateRequestDto dto);

}
