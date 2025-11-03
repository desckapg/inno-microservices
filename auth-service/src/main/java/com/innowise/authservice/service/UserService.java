package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.user.UserAuthDto;
import com.innowise.authservice.model.dto.user.UserAuthInfoDto;
import org.jspecify.annotations.NullMarked;

/**
 * Provides operations for retrieving users and managing their credentials.
 */
@NullMarked
public interface UserService {

  /**
   * Finds a user by unique identifier.
   *
   * @param id user identifier
   * @return user data
   */
  UserAuthDto findById(Long id);


  /**
   * Finds a user by login
   *
   * @param login user login
   * @return user data
   */
  UserAuthDto findByLogin(String login);

  /**
   * Finds a user that matches the given login and password.
   *
   * @param login user login
   * @param password raw password
   * @return user data
   */
  UserAuthDto findByLoginAndPassword(String login, String password);

  /**
   * Deletes credentials and user for the given user identifier.
   *
   * @param id user identifier
   */
  void delete(Long id);

  /**
   * Create new user and returns the created user.
   *
   * @param userDto user data payload
   * @return created user data
   */
  UserAuthInfoDto register(UserAuthInfoDto userDto);


}
