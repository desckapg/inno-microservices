package com.innowise.auth.test.security;

import com.innowise.auth.model.JwtUserDetails;
import com.innowise.auth.security.provider.AuthTokenProvider;
import com.innowise.auth.security.token.LoginRolesJwtAuthenticationToken;
import com.innowise.auth.test.annotation.WithMockCustomUser;
import com.innowise.auth.test.jwt.TestJwtTokenProvider;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@NullMarked
@RequiredArgsConstructor
public class TestUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

  private final AuthTokenProvider authTokenProvider;

  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    var userDetails = JwtUserDetails.builder()
            .id(annotation.id())
            .userId(annotation.userId())
            .login(annotation.login())
            .authorities(Arrays.stream(annotation.roles()).map(SimpleGrantedAuthority::new).toList())
            .build();
		var auth = new LoginRolesJwtAuthenticationToken(
        userDetails,
        TestJwtTokenProvider.genRandomSignedAccessToken(userDetails)
    );
    authTokenProvider.set(auth);
		context.setAuthentication(auth);
		return context;
  }
}
