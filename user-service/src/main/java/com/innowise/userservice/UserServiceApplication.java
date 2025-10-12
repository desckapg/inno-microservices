package com.innowise.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(
    exclude = {
        UserDetailsServiceAutoConfiguration.class
    }
)
public class UserServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }

}
