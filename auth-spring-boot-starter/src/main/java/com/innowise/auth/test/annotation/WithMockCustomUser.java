package com.innowise.auth.test.annotation;

import com.innowise.auth.test.security.TestUserSecurityContextFactory;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@WithSecurityContext(factory = TestUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

  long id() default 1L;

  long userId() default 1L;

  String login() default "user";

  String[] roles() default {"USER"};

}
