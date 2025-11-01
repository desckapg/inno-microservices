package com.innowise.orderservice.config;

import com.innowise.auth.security.filter.JwtAuthenticationFilter;
import com.innowise.auth.security.provider.AuthTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final AuthenticationManager authenticationManager;
  private final AuthTokenProvider authTokenProvider;

  @Bean
  @Profile("!test")
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(CsrfConfigurer::disable)
        .httpBasic(HttpBasicConfigurer::disable)
        .formLogin(FormLoginConfigurer::disable)
        .logout(LogoutConfigurer::disable)
        .authorizeHttpRequests(authz -> {
          authz.requestMatchers("/actuator/**").permitAll();
          authz.requestMatchers("/api/v1/orders/**").authenticated();
        })
        .addFilterAfter(new JwtAuthenticationFilter(
                PathPatternRequestMatcher.pathPattern("/api/v1/orders/**"),
                authenticationManager,
                authTokenProvider
            ), LogoutFilter.class
        );
    return http.build();
  }

}
