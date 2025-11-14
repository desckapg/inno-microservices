package com.innowise.paymentservice.integration.annotation;

import com.innowise.paymentservice.controller.kafka.consumer.OrderListener;
import com.innowise.paymentservice.controller.kafka.producer.PaymentProducer;
import com.innowise.paymentservice.repository.PaymentRepository;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
@MockitoSpyBean(types = {
    PaymentRepository.class,
    PaymentProducer.class,
    OrderListener.class
})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IT {

}
