package com.innowise.authservice.service.impl;

import com.innowise.authservice.exception.AuthFailedException;
import com.innowise.authservice.exception.ResourceAlreadyExistsException;
import com.innowise.authservice.exception.ResourceNotFoundException;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.user.UserDto;
import com.innowise.authservice.model.entity.Credentials;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.model.mapper.UserMapper;
import com.innowise.authservice.repository.UserRepository;
import com.innowise.authservice.service.UserService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@NullMarked
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserDto saveCredentials(CredentialDto credentialDto) {
    if (existsByLogin(credentialDto.login())) {
      throw ResourceAlreadyExistsException.byField("User", "login", credentialDto.login());
    }
    return userMapper.toDto(userRepository.save(
            User.builder()
                .credentials(Credentials.builder()
                    .login(credentialDto.login())
                    .passwordHash(passwordEncoder.encode(credentialDto.password()))
                    .build())
                .roles(Set.of(Role.USER))
                .build()
        )
    );
  }

  @Transactional
  public void deleteCredentials(Long id) {
    if (!userRepository.existsById(id)) {
      throw ResourceNotFoundException.byId("User", id);
    }
    userRepository.deleteById(id);
  }

  public boolean existsByLogin(String login) {
    return userRepository.existsByLogin(login);
  }

  public UserDto findByLogin(String login) {
    return userRepository.findByLogin(login)
        .map(userMapper::toDto)
        .orElseThrow(() -> ResourceNotFoundException.byField("User", "login", login));
  }

  public UserDto findByLoginAndPassword(String login, String password) {
    var user = userRepository.findByLogin(login)
        .orElseThrow(AuthFailedException::new);
    if (!passwordEncoder.matches(password, user.getCredentials().getPasswordHash())) {
      throw new AuthFailedException();
    }
    return userMapper.toDto(user);
  }

  public UserDto findById(Long id) {
    return userRepository.findById(id)
        .map(userMapper::toDto)
        .orElseThrow(() -> ResourceNotFoundException.byId("User", id));
  }
}
