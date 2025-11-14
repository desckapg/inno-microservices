package com.innowise.orderservice.controller.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.orderservice.model.dto.OrdersConstraints;
import com.innowise.orderservice.model.dto.order.OrderDto;
import com.innowise.orderservice.model.dto.order.OrderSpecsDto;
import com.innowise.orderservice.model.enums.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@NullMarked
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;

  @GetMapping("/{id}")
  public ResponseEntity<OrderDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.findById(id));
  }

  @GetMapping
  public ResponseEntity<List<OrderDto>> findAll(
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) List<Long> ids,
      @RequestParam(required = false) List<OrderStatus> statuses
  ) {
    return ResponseEntity.ok(orderService.findAll(OrderSpecsDto.builder()
        .userId(userId)
        .ids(ids)
        .statuses(statuses)
        .build())
    );
  }

  @PostMapping
  public ResponseEntity<OrderDto> create(
      @RequestBody
      @Validated(value = OrdersConstraints.Create.class)
      @JsonView(OrdersConstraints.Create.class)
      OrderDto orderDto
  ) {
    var createdOrderDto = orderService.create(orderDto);
    return ResponseEntity.created(URI.create("api/v1/orders/" + createdOrderDto.id()))
        .body(createdOrderDto);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.innowise.auth.model.Role).MANAGER)")
  public ResponseEntity<OrderDto> update(
      @PathVariable Long id,
      @RequestBody
      @Validated(value = OrdersConstraints.Update.class)
      OrderDto orderDto
  ) {
    return ResponseEntity.ok(orderService.update(id, orderDto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.innowise.auth.model.Role).MANAGER)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    orderService.delete(id);
    return ResponseEntity.noContent()
        .location(URI.create("api/v1/orders/" + id))
        .build();
  }

}
