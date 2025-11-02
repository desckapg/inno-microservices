package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.entity.Payment;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

@NullMarked
public interface PaymentRepository extends MongoRepository<Payment, String> {

  List<Payment> findByOrderId(Long orderId);

  List<Payment> findByUserId(Long paymentId);

  List<Payment> findByStatusIn(List<Payment.Status> statuses);

  @Query("{timestamp: { $and: [{ $gte: ?0 }, { $lte: ?1 }] }, status: { $eq: 'SUCCEEDED' } }")
  BigDecimal findTotalSum(ZonedDateTime startDate, ZonedDateTime endDate);

}
