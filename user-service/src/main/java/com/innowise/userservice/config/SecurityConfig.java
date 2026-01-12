package com.innowise.userservice.config;

import com.innowise.auth.security.filter.JwtAuthenticationFilter;
import com.innowise.auth.security.provider.AuthTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuthenticationManager authenticationManager;
  private final AuthTokenProvider authTokenProvider;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {

    RequestMatcher requiresAuth = new AndRequestMatcher(
        new NegatedRequestMatcher(
            PathPatternRequestMatcher.pathPattern("/actuator/**")
        ),
        new NegatedRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/users")
        ),
        PathPatternRequestMatcher.pathPattern("/api/v1/**")
    );

    http
        .sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(CsrfConfigurer::disable)
        .httpBasic(HttpBasicConfigurer::disable)
        .formLogin(FormLoginConfigurer::disable)
        .logout(LogoutConfigurer::disable)
        .authorizeHttpRequests(authz ->
            authz
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
        )
        .addFilterAfter(new JwtAuthenticationFilter(
            requiresAuth,
            authenticationManager,
            authTokenProvider
        ), LogoutFilter.class);
    return http.build();
  }

}
