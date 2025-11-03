package com.innowise.auth.autoconfigure;

import com.innowise.auth.model.Role;
import com.innowise.auth.security.JwtAuthenticationProvider;
import com.innowise.auth.security.provider.AuthTokenProvider;
import com.innowise.auth.security.provider.AuthTokenProviderInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({
    OncePerRequestFilter.class,
    SecurityFilterChain.class,
    HttpSecurity.class
})
@EnableWebMvc
public class AuthAutoConfiguration implements WebMvcConfigurer {

  @Bean
  @ConditionalOnMissingBean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withRolePrefix("")
        .role(Role.SUPER_ADMIN.getAuthority()).implies(Role.ADMIN.getAuthority())
        .role(Role.ADMIN.getAuthority()).implies(Role.MANAGER.getAuthority())
        .role(Role.MANAGER.getAuthority()).implies(Role.USER.getAuthority())
        .build();
  }

  @Bean
  public JwtAuthenticationProvider jwtAuthenticationProvider() {
    return new JwtAuthenticationProvider();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      JwtAuthenticationProvider jwtAuthenticationProvider) {
    return new ProviderManager(jwtAuthenticationProvider);
  }

  @Bean
  public AuthTokenProvider authTokenProvider() {
    return new AuthTokenProvider();
  }

  @Bean
  public AuthTokenProviderInterceptor authTokenProviderInterceptor(
      AuthTokenProvider authTokenProvider) {
    return new AuthTokenProviderInterceptor(authTokenProvider);
  }


}
