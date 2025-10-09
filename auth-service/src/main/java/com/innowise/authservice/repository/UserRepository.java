package com.innowise.authservice.repository;

import com.innowise.authservice.model.entity.User;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@NullMarked
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.credentials.login = :login")
  boolean existsByLogin(String login);

  @Query("SELECT u FROM User u WHERE u.credentials.login = :login")
  Optional<User> findByLogin(String login);

}
