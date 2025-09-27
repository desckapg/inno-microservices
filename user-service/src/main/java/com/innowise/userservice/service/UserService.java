package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.user.UserDto;
import java.util.List;

/**
 * Service responsible for managing users.
 * Provides CRUD operations, lookup by email, and batch retrieval by identifiers.
 */
public interface UserService {

  /**
   * Creates a new user.
   * @param dto user data
   * @return persisted user
   */
  UserDto create(UserDto dto);

  /**
   * Updates an existing user.
   * @param id user identifier
   * @param dto new data
   * @return updated user
   */
  UserDto update(Long id, UserDto dto);

  /**
   * Deletes a user by id.
   * @param id user identifier
   */
  void delete(Long id);

  /**
   * Finds a user by id.
   * @param id user identifier
   * @return user DTO
   */
  UserDto findById(Long id);

  /**
   * Finds a user by email.
   * @param email user email
   * @return user DTO
   */
  UserDto findByEmail(String email);

  /**
   * Retrieves users by a list of identifiers.
   * @param ids list of user ids
   * @return list of users
   */
  List<UserDto> findAllByIdIn(List<Long> ids);

  /**
   * Returns all users.
   * @return list of users
   */
  List<UserDto> findAll();

}
