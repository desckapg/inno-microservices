package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@NullMarked
public interface OrderRepository extends JpaRepository<Order, Long>,
    JpaSpecificationExecutor<Order> {

  @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
  Optional<Order> findById(Long id);

  @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
  List<Order> findAll(Specification<Order> spec);

  @Query("SELECT o.userId FROM Order o WHERE o.id = :id")
  Optional<String> findUserIdById(Long id);

}
