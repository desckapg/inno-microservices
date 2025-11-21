package com.innowise.common.model.event;

import java.util.UUID;
import lombok.Getter;

@Getter
public abstract class BaseEvent {

  protected UUID eventId = UUID.randomUUID();

}
