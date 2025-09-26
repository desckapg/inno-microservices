package com.innowise.userservice.service;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.ResourceAlreadyExistsException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.model.dto.user.UserCreateRequestDto;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.dto.user.UserUpdateRequestDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final CacheHelper cacheHelper;

  @Transactional
  public UserDto create(UserCreateRequestDto dto) {
    if (userRepository.existsByEmail(dto.email())) {
      throw ResourceAlreadyExistsException.byField("User", "email", dto.email());
    }
    return userMapper.toDto(
        userRepository.save(
            userMapper.toEntity(dto)
        )
    );
  }

  @Transactional
  public UserDto update(Long id, UserUpdateRequestDto dto) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> ResourceNotFoundException.byId("User", id));

    if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
      throw ResourceAlreadyExistsException.byField("User", "email", dto.email());
    }

    user.setName(dto.name());
    user.setSurname(dto.surname());
    user.setEmail(dto.email());
    user.setBirthDate(dto.birthDate());

    var savedUser = userRepository.save(user);
    cacheHelper.updateUserCaches(savedUser);

    return userMapper.toDto(savedUser);

  }

  @Transactional
  @CacheEvict(value = CacheHelper.USER_CACHE, key = "#id")
  public void delete(Long id) {
    userRepository.delete(
        userRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.byId("User", id))
    );
  }

  @Cacheable(value = CacheHelper.USER_CACHE, key = "#id")
  public UserDto findById(Long id) {
    return userRepository.findWithCardsById(id)
        .map(userMapper::toDto)
        .orElseThrow(() -> ResourceNotFoundException.byId("User", id));
  }

  public UserDto findByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(userMapper::toDto)
        .orElseThrow(() -> ResourceNotFoundException.byField("User", "email", email));
  }

  public List<UserDto> findAllByIdIn(List<Long> ids) {
    return userRepository.findAllByIdIn(ids)
        .stream()
        .map(userMapper::toDto)
        .toList();
  }

}
