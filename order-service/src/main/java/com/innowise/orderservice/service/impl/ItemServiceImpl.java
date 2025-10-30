package com.innowise.orderservice.service.impl;

import com.innowise.common.exception.ResourceNotFoundException;
import com.innowise.orderservice.model.dto.item.ItemDto;
import com.innowise.orderservice.model.mapper.ItemMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;

  @Override
  public ItemDto findById(Long id) {
    return itemRepository.findById(id)
        .map(itemMapper::toDto)
        .orElseThrow(() -> ResourceNotFoundException.byId("Item", id));
  }

  @Override
  public List<ItemDto> findAll() {
    return itemRepository.findAll().stream()
        .map(itemMapper::toDto)
        .toList();
  }

  @Override
  public List<ItemDto> findByIdIn(List<Long> ids) {
    return itemRepository.findAllById(ids).stream()
        .map(itemMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public ItemDto create(ItemDto itemDto) {
    return itemMapper.toDto(itemRepository.save(itemMapper.toEntity(itemDto)));
  }

  @Override
  @Transactional
  public ItemDto update(Long id, ItemDto itemDto) {
    var item = itemRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.byId("Item", id));
    item.setName(itemDto.name());
    item.setPrice(itemDto.price());
    return itemMapper.toDto(itemRepository.save(item));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    itemRepository.deleteById(id);
  }
}
