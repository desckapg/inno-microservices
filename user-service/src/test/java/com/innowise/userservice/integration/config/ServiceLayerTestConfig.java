package com.innowise.userservice.integration.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableJpaRepositories(basePackages = "com.innowise.userservice.repository")
@EntityScan(basePackages = "com.innowise.userservice.model.entity")
@ComponentScan(
    basePackages = "com.innowise.userservice",
    useDefaultFilters = false,
    includeFilters = {
        @Filter(type = FilterType.ANNOTATION, classes = {Service.class, Repository.class, Component.class})
    },
    excludeFilters = {
        @Filter(type = FilterType.ANNOTATION, classes = {Controller.class, RestController.class})
    }
)
public class ServiceLayerTestConfig {

}
