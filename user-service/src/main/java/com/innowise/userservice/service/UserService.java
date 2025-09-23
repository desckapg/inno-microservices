package com.innowise.userservice.service;

import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.exception.UserWithEmailExistsException;
import com.innowise.userservice.model.dto.user.UserCreateRequestDto;
import com.innowise.userservice.model.dto.user.UserResponseDto;
import com.innowise.userservice.model.dto.user.UserUpdateRequestDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  public UserResponseDto create(UserCreateRequestDto dto) {
    if (userRepository.existsByEmail(dto.email())) {
      throw new UserWithEmailExistsException(dto.email());
    }
    return userMapper.toDto(userRepository.save(userMapper.toEntity(dto)));
  }

  @Transactional
  public UserResponseDto update(Long id, UserUpdateRequestDto dto) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    user.setName(dto.name());
    user.setSurname(dto.surname());
    if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
      throw new UserWithEmailExistsException(dto.email());
    }
    user.setEmail(dto.email());
    user.setBirthDate(dto.birthDate());
    return userMapper.toDto(userRepository.save(user));
  }

  @Transactional
  public void delete(Long id) {
    userRepository.delete(
        userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id))
    );
  }

  public UserResponseDto findById(Long id, boolean includeCards) {
    if (includeCards) {
      return userRepository.findWithCardsById(id)
          .map(userMapper::toWithCardsDto)
          .orElseThrow(() -> new UserNotFoundException(id));
    }
    return userRepository.findById(id)
        .map(userMapper::toDto)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  public UserResponseDto findByEmail(String email, boolean includeCards) {
    if (includeCards) {
      return userRepository.findWithCardsByEmail(email)
          .map(userMapper::toWithCardsDto)
          .orElseThrow(() -> new UserNotFoundException(email));

    }
    return userRepository.findByEmail(email)
        .map(userMapper::toDto)
        .orElseThrow(() -> new UserNotFoundException(email));
  }

  public List<UserResponseDto> findAllByIdIn(List<Long> ids, boolean includeCards) {
    if (includeCards) {
      return userRepository.findAllWithCardsByIdIn(ids)
        .stream()
        .map(userMapper::toWithCardsDto)
        .toList();
    }
    return userRepository.findAllByIdIn(ids)
        .stream()
        .map(userMapper::toDto)
        .toList();
  }

}
