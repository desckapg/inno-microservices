package com.innowise.orderservice.controller.rest;

import com.innowise.orderservice.model.dto.ItemsConstraints;
import com.innowise.orderservice.model.dto.item.ItemDto;
import com.innowise.orderservice.service.ItemService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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

@Validated
@RequiredArgsConstructor
@NullMarked
@RestController
@RequestMapping("/api/v1/orders/items")
public class ItemController {

  private final ItemService itemService;

  @GetMapping
  @PreAuthorize("hasAuthority(T(com.innowise.auth.model.Role).USER)")
  public ResponseEntity<List<ItemDto>> findAll(
      @RequestParam(required = false) @Nullable List<Long> ids) {
    List<ItemDto> items;
    if (ids == null) {
      items = itemService.findAll();
    } else {
      items = itemService.findByIdIn(ids);
    }
    return ResponseEntity.ok(items);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.innowise.auth.model.Role).USER)")
  public ResponseEntity<ItemDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(itemService.findById(id));
  }

  @PostMapping
  @PreAuthorize("hasAuthority(T(com.innowise.auth.model.Role).MANAGER)")
  public ResponseEntity<ItemDto> create(
      @Validated(value = ItemsConstraints.Create.class)
      @RequestBody
      ItemDto itemDto) {
    var createdItemDto = itemService.create(itemDto);
    return ResponseEntity
        .created(URI.create("api/v1/orders/items/" + createdItemDto.id()))
        .body(createdItemDto);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.innowise.auth.model.Role).MANAGER)")
  public ResponseEntity<ItemDto> update(
      @PathVariable Long id,
      @Validated(value = ItemsConstraints.Update.class)
      @RequestBody
      ItemDto itemDto
  ) {
    return ResponseEntity.ok(itemService.update(id, itemDto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.innowise.auth.model.Role).MANAGER)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    itemService.delete(id);
    return ResponseEntity.noContent()
        .location(URI.create("api/v1/orders/items/" + id))
        .build();
  }
}
