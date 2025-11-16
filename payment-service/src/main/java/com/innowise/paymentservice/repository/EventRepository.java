package com.innowise.paymentservice.repository;

public interface EventRepository {

  boolean isEventProcessed(String consumerGroupId, String eventId);

  void saveProcessedEvent(String consumerGroupId, String eventId);

}
