package com.innowise.orderservice.model.dto.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.innowise.orderservice.model.dto.ItemsConstraints;
import com.innowise.orderservice.model.dto.OrdersConstraints;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemDto(

    @NotNull(
        groups = {
            OrdersConstraints.Create.class
        },
        message = "Id must be provided"
    )
    @JsonView({OrdersConstraints.Create.class})
    Long id,

    @NotBlank(
        groups = {
            ItemsConstraints.Update.class
        },
        message = "Name must be provided"
    )
    String name,

    @NotNull(
        groups = {
            ItemsConstraints.Create.class
        },
        message = "Price must be provided"
    )
    @DecimalMin(
        groups = {
            ItemsConstraints.Create.class
        },
        value = "0.0",
        message = "Price must be greater than 0 or equal"
    )
    BigDecimal price

) implements Serializable {

    // Sometimes Hibernate BigDecimal type convertor can add trailing zeros,
    // so with just equals "43.10" and "43.1" big decimals will be not equal.
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemDto itemDto = (ItemDto) o;

        boolean idAndNameEquality = Objects.equals(id(), itemDto.id())
            && Objects.equals(name(), itemDto.name());

        if (price == null && itemDto.price() == null) {
            return idAndNameEquality;
        }

        if (price != null && itemDto.price() != null) {
            return idAndNameEquality && price.compareTo(itemDto.price()) == 0;
        }

        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id(), name(), price());
    }
}
