package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findByIdIn(List<Long> ids);

  List<Order> findByStatusIn(List<Order.Status> statuses);

}
