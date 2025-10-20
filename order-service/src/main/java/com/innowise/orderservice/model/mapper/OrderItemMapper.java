package com.innowise.orderservice.model.mapper;

import com.innowise.orderservice.model.dto.orderitem.OrderItemDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.OrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(
    componentModel = "spring",
    injectionStrategy = org.mapstruct.InjectionStrategy.CONSTRUCTOR,
    uses = {ItemMapper.class}
)
public abstract class OrderItemMapper {

  @PersistenceContext
  private EntityManager em;

  abstract OrderItemDto toDto(OrderItem orderItem);

  @Named("toDtoIdOnly")
  @Mapping(target = "item", qualifiedByName = "toDtoIdOnly")
  abstract OrderItemDto toDtoIdOnly(OrderItem orderItem);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "order", ignore = true)
  abstract OrderItem toEntity(OrderItemDto orderItem);

  @AfterMapping
  void linkItem(@MappingTarget OrderItem orderItem) {
    orderItem.setItem(em.getReference(Item.class, orderItem.getItem().getId()));
  }

}
