package com.innowise.paymentservice.repository.impl;

import com.innowise.paymentservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisEventRepositoryImpl implements EventRepository {

  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public boolean isEventProcessed(String consumerGroupId, String eventId) {
    return redisTemplate.opsForSet().isMember(consumerGroupId, eventId);
  }

  @Override
  public void saveProcessedEvent(String consumerGroupId, String eventId) {
    redisTemplate.opsForSet().add(consumerGroupId, eventId);
  }

}
