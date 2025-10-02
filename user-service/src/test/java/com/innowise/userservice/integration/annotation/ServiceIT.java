package com.innowise.userservice.integration.annotation;

import com.innowise.userservice.integration.config.ServiceLayerTestConfig;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.service.UserService;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(classes = {ServiceLayerTestConfig.class})
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MockitoSpyBean(types = {
    UserRepository.class,
    CardRepository.class,
    UserService.class,
    CardService.class
})
public @interface ServiceIT {
}
