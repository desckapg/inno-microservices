package com.innowise.auth.autoconfigure;

import com.innowise.auth.model.Role;
import com.innowise.auth.security.filter.JwtAuthenticationFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({
    OncePerRequestFilter.class,
    SecurityFilterChain.class,
    HttpSecurity.class
})
public class AuthAutoConfiguration {

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
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager();
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtAuthenticationFilter jwtExtractorFilter() {
    return new JwtAuthenticationFilter();
  }

  @Bean
  @ConditionalOnMissingBean
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
      JwtAuthenticationFilter filter) throws Exception {
    http.sessionManagement(AbstractHttpConfigurer::disable);
    http.csrf(AbstractHttpConfigurer::disable);
    http.addFilterBefore(filter, BasicAuthenticationFilter.class);
    return http.build();
  }

}
