package com.innowise.orderservice.model.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrdersConstraints {

  public interface Base { }

  public interface Create extends Base { }

  public interface Update extends Base { }

  public interface Find extends Base { }

  public interface List extends Base { }

}
