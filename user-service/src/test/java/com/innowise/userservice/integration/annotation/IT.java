package com.innowise.userservice.integration.annotation;

import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.service.UserService;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestEntityManager
@ExtendWith(MockitoExtension.class)
@MockitoSpyBean(types = {
    UserRepository.class,
    CardRepository.class,
    UserService.class,
    CardService.class
})
@AutoConfigureMockMvc
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IT {

}
