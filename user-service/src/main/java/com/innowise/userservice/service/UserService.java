package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.user.UserDto;
import java.util.List;

public interface UserService {

  UserDto create(UserDto dto);

  UserDto update(Long id, UserDto dto);

  void delete(Long id);

  UserDto findById(Long id);

  UserDto findByEmail(String email);

  List<UserDto> findAllByIdIn(List<Long> ids);

  List<UserDto> findAll();

}
