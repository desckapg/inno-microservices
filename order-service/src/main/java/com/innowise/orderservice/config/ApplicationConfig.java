package com.innowise.orderservice.config;

import com.innowise.auth.security.interceptor.JwtAuthInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

	@Bean
	public RequestInterceptor jwtAuthInterceptor() {
		return new JwtAuthInterceptor();
	}

}
