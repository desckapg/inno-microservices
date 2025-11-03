package com.innowise.paymentservice.model.dto.orderitem;

import com.innowise.paymentservice.model.dto.item.ItemDto;
import java.io.Serializable;

public record OrderItemDto(

    ItemDto item,

    Integer quantity

) implements Serializable {

}
