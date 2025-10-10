package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.user.UserDto;
import com.innowise.common.exception.ResourceNotFoundException;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Provides operations for retrieving users and managing their credentials.
 */
@NullMarked
public interface UserService extends UserDetailsService {

  /**
   * Finds a user by unique identifier.
   *
   * @param id user identifier
   * @return user data
   */
  UserDto findById(Long id);


  /**
   * Finds a user by login
   *
   * @param login user login
   * @return user data
   */
  UserDto findByLogin(String login);

  /**
   * Finds a user that matches the given login and password.
   *
   * @param login user login
   * @param password raw password
   * @return user data
   */
  UserDto findByLoginAndPassword(String login, String password);

  /**
   * Persists user credentials and returns the created user.
   *
   * @param credentialDto credentials payload
   * @return user data
   */
  UserDto saveCredentials(CredentialDto credentialDto);

  /**
   * Deletes credentials and user for the given user identifier.
   *
   * @param id user identifier
   */
  void deleteCredentials(Long id);

  @Override
  default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    try {
      return findByLogin(username);
    } catch (ResourceNotFoundException e) {
      throw new UsernameNotFoundException(e.getMessage());
    }
  }

}
