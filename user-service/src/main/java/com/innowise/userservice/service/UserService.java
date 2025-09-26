package com.innowise.userservice.service;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.model.dto.user.UserCreateRequestDto;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.dto.user.UserUpdateRequestDto;
import com.innowise.userservice.model.dto.user.UserWithCardsDto;
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
      throw new UserAlreadyExistsException(dto.email());
    }
    return userMapper.toDto(
        userRepository.save(
            userMapper.toEntity(dto)
        )
    );
  }

  @Transactional
  @CacheEvict(
      value = {CacheHelper.USER_BASIC_CACHE, CacheHelper.USER_WITH_CARDS_CACHE},
      key = "#id",
      beforeInvocation = true
  )
  public UserDto update(Long id, UserUpdateRequestDto dto) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
      throw new UserAlreadyExistsException(dto.email());
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
  public void delete(Long id) {
    userRepository.delete(
        userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id))
    );
    cacheHelper.evictUserCaches(id);
  }

  @Cacheable(value = CacheHelper.USER_BASIC_CACHE, key = "#id")
  public UserDto findById(Long id) {
    return userRepository.findById(id)
        .map(userMapper::toDto)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  @Cacheable(value = CacheHelper.USER_WITH_CARDS_CACHE, key = "#id")
  public UserWithCardsDto findWithCardsById(Long id) {
    return userRepository.findWithCardsById(id)
        .map(userMapper::toWithCardsDto)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  public UserDto findByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(userMapper::toDto)
        .orElseThrow(() -> new UserNotFoundException(email));
  }

  public List<UserDto> findAllByIdIn(List<Long> ids) {
    return userRepository.findAllByIdIn(ids)
        .stream()
        .map(userMapper::toDto)
        .toList();
  }

  public List<UserWithCardsDto> findWithCardsAllByIdIn(List<Long> ids) {
    return userRepository.findAllWithCardsByIdIn(ids)
        .stream()
        .map(userMapper::toWithCardsDto)
        .toList();
  }

}
