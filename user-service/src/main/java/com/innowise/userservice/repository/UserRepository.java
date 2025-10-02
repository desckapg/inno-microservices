package com.innowise.userservice.repository;

import  com.innowise.userservice.model.entity.User;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface UserRepository extends JpaRepository<User, Long> {

  @EntityGraph(attributePaths = "cards")
  Optional<User> findWithCardsById(Long id);

  @EntityGraph(attributePaths = "cards")
  List<User> findAllByIdIn(List<Long> ids);

  @EntityGraph(attributePaths = "cards")
  List<User> findAll();

  @EntityGraph(attributePaths = "cards")
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

}
