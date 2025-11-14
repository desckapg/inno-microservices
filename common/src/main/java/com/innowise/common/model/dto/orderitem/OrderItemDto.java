package com.innowise.common.model.dto.orderitem;

import com.innowise.common.model.dto.item.ItemDto;
import java.io.Serializable;

public record OrderItemDto(

    ItemDto item,

    Integer quantity

) implements Serializable {

}
