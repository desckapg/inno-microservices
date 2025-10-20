package com.innowise.orderservice.model.dto.order;

import com.innowise.orderservice.model.entity.Order;
import java.util.LinkedList;
import java.util.List;
import lombok.Builder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

@NullMarked
@Builder
public record OrderSpecsDto(
    @Nullable List<Long> ids,
    @Nullable List<Order.Status> statuses,
    @Nullable Long userId
) {

  public Specification<Order> convertToSpecification() {
    List<Specification<Order>> specifications = new LinkedList<>();

    if (ids != null) {
      specifications.add((root, _, _) -> root.get("id").in(ids));
    }

    if (statuses != null) {
      specifications.add((root, _, _) -> root.get("status").in(statuses));
    }

    if (userId != null) {
      specifications.add((root, _, builder) -> builder.equal(root.get("userId"), userId));
    }

    return Specification.allOf(specifications);
  }

}
