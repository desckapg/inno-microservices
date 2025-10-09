package com.innowise.auth.autoconfigure;

import com.innowise.auth.filter.JwtExtractorFilter;
import com.innowise.auth.model.Role;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({
    OncePerRequestFilter.class,
    SecurityFilterChain.class,
    JwtExtractorFilter.class
})
@ConditionalOnBean({
    HttpSecurity.class,
})
public class AuthAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role(Role.SUPER_ADMIN.getAuthority()).implies(Role.ADMIN.getAuthority())
        .role(Role.ADMIN.getAuthority()).implies(Role.MANAGER.getAuthority())
        .role(Role.MANAGER.getAuthority()).implies(Role.USER.getAuthority())
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtExtractorFilter jwtExtractorFilter() {
    return new JwtExtractorFilter();
  }

  @Bean
  @ConditionalOnMissingBean
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtExtractorFilter filter) throws Exception {
    http.addFilterBefore(filter, SecurityContextHolderFilter.class);
    return http.build();
  }
}
