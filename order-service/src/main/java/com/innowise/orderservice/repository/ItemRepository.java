package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Item;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface ItemRepository extends JpaRepository<Item, Long> {

}
