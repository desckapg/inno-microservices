package com.innowise.orderservice.model.mapper;

import com.innowise.orderservice.model.dto.item.ItemDto;
import com.innowise.orderservice.model.entity.Item;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ItemMapper {

  ItemDto toDto(Item item);

  @Named("toDtoIdOnly")
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "price", ignore = true)
  ItemDto toDtoIdOnly(Item item);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Item toEntity(ItemDto itemDto);

}
