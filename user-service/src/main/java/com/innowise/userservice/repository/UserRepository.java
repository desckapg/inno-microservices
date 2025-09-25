package com.innowise.userservice.repository;

import com.innowise.userservice.model.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u WHERE u.id IN (:ids)")
  List<User> findAllByIdIn(List<Long> ids);

  Optional<User> findByEmail(String email);

}
