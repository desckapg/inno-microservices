package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@NullMarked
public interface OrderRepository extends JpaRepository<Order, Long>,
    JpaSpecificationExecutor<Order> {



  @Query("SELECT o.userId FROM Order o WHERE o.id = :id")
  Optional<Long> findUserIdById(Long id);

}
