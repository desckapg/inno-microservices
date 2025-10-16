package com.innowise.orderservice.model.mapper;

import com.innowise.orderservice.model.dto.item.ItemDto;
import com.innowise.orderservice.model.entity.Item;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ItemMapper {

  ItemDto toDto(Item item);

  Item toEntity(ItemDto itemDto);

}
