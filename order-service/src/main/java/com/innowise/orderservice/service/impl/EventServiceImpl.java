package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.repository.EventRepository;
import com.innowise.orderservice.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

  private final EventRepository eventRepository;

  @Override
  public boolean isEventProcessed(String consumerGroupId, String eventId) {
    return eventRepository.isEventProcessed(consumerGroupId, eventId);
  }

  @Override
  public void saveProcessedEvent(String consumerGroupId, String eventId) {
    eventRepository.saveProcessedEvent(consumerGroupId, eventId);
  }
}
