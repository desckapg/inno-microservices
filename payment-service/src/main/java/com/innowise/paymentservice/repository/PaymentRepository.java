package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.entity.Payment;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.MongoRepository;

@NullMarked
public interface PaymentRepository extends MongoRepository<Payment, String> {

}
