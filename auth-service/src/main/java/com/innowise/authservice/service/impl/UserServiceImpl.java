package com.innowise.authservice.service.impl;

import com.innowise.authservice.exception.AuthFailedException;
import com.innowise.authservice.model.dto.user.UserAuthDto;
import com.innowise.authservice.model.dto.user.UserAuthInfoDto;
import com.innowise.authservice.model.entity.Credentials;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.model.mapper.UserMapper;
import com.innowise.authservice.repository.UserRepository;
import com.innowise.authservice.service.UserService;
import com.innowise.authservice.service.client.UserServiceClient;
import com.innowise.common.exception.ResourceAlreadyExistsException;
import com.innowise.common.exception.ResourceNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@NullMarked
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserServiceClient userServiceClient;

  @Override
  @Transactional
  public void delete(Long id) {
    userRepository.findById(id)
        .ifPresent(user -> {
          userRepository.deleteById(id);
          userServiceClient.delete(user.getUserId());
        });

  }

  @Override
  @Transactional
  public UserAuthInfoDto register(UserAuthInfoDto userAuthInfoDto) {
    var createdUserInfoDto = userServiceClient.create(userAuthInfoDto.infoDto());

    try {
      var createdUserAuthDto = saveCredentials(UserAuthDto.builder()
          .credentials(userAuthInfoDto.authDto().credentials())
          .roles(Set.of(Role.USER.getAuthority()))
          .userId(createdUserInfoDto.id())
          .build());
      return UserAuthInfoDto.builder()
          .authDto(createdUserAuthDto)
          .infoDto(createdUserInfoDto)
          .build();
    } catch (Exception ex) {
      userServiceClient.delete(createdUserInfoDto.id());
      throw ex;
    }
  }

  private UserAuthDto saveCredentials(UserAuthDto userAuthDto) {
    if (existsByLogin(userAuthDto.credentials().login())) {
      throw ResourceAlreadyExistsException.byField("User", "login",
          userAuthDto.credentials().login());
    }
    return userMapper.toDto(userRepository.save(User.builder()
        .credentials(Credentials.builder()
            .login(userAuthDto.credentials().login())
            .passwordHash(passwordEncoder.encode(userAuthDto.credentials().password()))
            .build()
        )
        .userId(userAuthDto.userId())
        .roles(userAuthDto.roles().stream().map(Role::valueOf).collect(Collectors.toSet()))
        .build()
    ));
  }

  public boolean existsByLogin(String login) {
    return userRepository.existsByLogin(login);
  }

  public UserAuthDto findByLogin(String login) {
    return userRepository.findByLogin(login)
        .map(userMapper::toDto)
        .orElseThrow(() -> ResourceNotFoundException.byField("User", "login", login));
  }

  public UserAuthDto findByLoginAndPassword(String login, String password) {
    var user = userRepository.findByLogin(login)
        .orElseThrow(AuthFailedException::new);
    if (!passwordEncoder.matches(password, user.getCredentials().getPasswordHash())) {
      throw new AuthFailedException();
    }
    return userMapper.toDto(user);
  }

  public UserAuthDto findById(Long id) {
    return userRepository.findById(id)
        .map(userMapper::toDto)
        .orElseThrow(() -> ResourceNotFoundException.byId("User", id));
  }
}
