package com.innowise.paymentservice.model.mapper;

import com.innowise.paymentservice.model.dto.payment.PaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    componentModel = "spring"
)
public interface PaymentMapper {

  Payment toEntity(PaymentDto paymentDto);

  PaymentDto toDto(Payment payment);

}
