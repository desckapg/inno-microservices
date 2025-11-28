package com.innowise.paymentservice.service;

public interface EventService {

  boolean isEventProcessed(String consumerGroupId, String eventId);

  void saveProcessedEvent(String consumerGroupId, String eventId);

}
