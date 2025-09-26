package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  @EntityGraph(attributePaths = "cards")
  Optional<User> findWithCardsById(Long id);

  @EntityGraph(attributePaths = "cards")
  List<User> findAllByIdIn(List<Long> ids);

  @EntityGraph(attributePaths = "cards")
  List<User> findAllWithCardsByIdIn(List<Long> ids);

  @EntityGraph(attributePaths = "cards")
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

}
