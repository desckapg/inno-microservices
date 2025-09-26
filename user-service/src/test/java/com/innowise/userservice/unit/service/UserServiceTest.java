package com.innowise.userservice.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.model.dto.user.UserCreateRequestDto;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.dto.user.UserUpdateRequestDto;
import com.innowise.userservice.model.dto.user.UserWithCardsDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.model.mapper.CardMapperImpl;
import com.innowise.userservice.model.mapper.UserMapper;
import com.innowise.userservice.model.mapper.UserMapperImpl;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.testutil.Users;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private CacheHelper cacheManager;

  private final CardMapper cardMapper = new CardMapperImpl();
  private final UserMapper userMapper = new UserMapperImpl(cardMapper);

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, userMapper, cacheManager);
  }

  @Test
  void create_whenValidData_shouldCreateUser() {
    UserCreateRequestDto createDto = new UserCreateRequestDto(
        "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com"
    );
    User savedUser = Users.buildWithId(1L, "John", "Doe", "john@example.com");

    when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserDto result = userService.create(createDto);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("John");
    assertThat(result.surname()).isEqualTo("Doe");
    assertThat(result.email()).isEqualTo("john@example.com");
    verify(userRepository).existsByEmail("john@example.com");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void create_whenEmailExists_shouldThrowUserWithEmailExistsException() {
    UserCreateRequestDto createDto = new UserCreateRequestDto(
        "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com"
    );

    when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.create(createDto))
        .isInstanceOf(UserAlreadyExistsException.class);

    verify(userRepository).existsByEmail("john@example.com");
    verify(userRepository, never()).save(any());
  }

  @Test
  void update_whenValidData_shouldUpdateUser() {
    Long userId = 1L;
    UserUpdateRequestDto updateDto = new UserUpdateRequestDto(
        "Jane", "Smith", "jane@example.com",
        LocalDate.of(1991, 2, 2)
    );
    User existingUser = Users.buildWithId(userId, "John", "Doe", "john@example.com");
    User updatedUser = Users.buildWithId(userId, "Jane", "Smith", "jane@example.com");
    updatedUser.setBirthDate(LocalDate.of(1991, 2, 2));

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);

    UserDto result = userService.update(userId, updateDto);

    assertThat(result.name()).isEqualTo("Jane");
    assertThat(result.surname()).isEqualTo("Smith");
    assertThat(result.email()).isEqualTo("jane@example.com");
    assertThat(result.birthDate()).isEqualTo(LocalDate.of(1991, 2, 2));
    verify(userRepository).save(any(User.class));
  }

  @Test
  void update_whenUserNotFound_shouldThrowUserNotFoundException() {
    Long userId = 1L;
    UserUpdateRequestDto updateDto = new UserUpdateRequestDto(
        "Jane", "Smith", "jane@example.com",
        LocalDate.of(1991, 2, 2)
    );

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(userId, updateDto))
        .isInstanceOf(UserNotFoundException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void update_whenEmailExistsForAnotherUser_shouldThrowUserWithEmailExistsException() {
    Long userId = 1L;
    UserUpdateRequestDto updateDto = new UserUpdateRequestDto(
        "Jane", "Smith", "jane@example.com", LocalDate.of(1991, 2, 2)
    );
    User existingUser = Users.buildWithId(userId, "John", "Doe", "john@example.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.update(userId, updateDto))
        .isInstanceOf(UserAlreadyExistsException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void delete_whenUserExists_shouldDeleteUser() {
    Long userId = 1L;
    User user = Users.buildWithId(userId, "John", "Doe", "john@example.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.delete(userId);

    verify(userRepository).delete(user);
  }

  @Test
  void delete_whenUserNotFound_shouldThrowUserNotFoundException() {
    Long userId = 1L;

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);

    verify(userRepository, never()).delete(any());
  }

  @Test
  void findById_whenUserExists_shouldReturnUser() {
    Long userId = 1L;
    User user = Users.buildWithId(userId, "John", "Doe", "john@example.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    UserDto result = userService.findById(userId);

    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.name()).isEqualTo("John");
    assertThat(result.surname()).isEqualTo("Doe");
    assertThat(result.email()).isEqualTo("john@example.com");
  }

  @Test
  void findById_whenUserNotFound_shouldThrowUserNotFoundException() {
    var user = Users.build();

    when(userRepository.findWithCardsById(user.getId()))
        .thenReturn(Optional.empty());

    var id = user.getId();
    assertThatThrownBy(() -> userService.findWithCardsById(id))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void findWithCardsById_whenUserExists_shouldReturnUserWithCards() {
    Long userId = 1L;
    User user = Users.buildWithId(userId, "John", "Doe", "john@example.com");

    when(userRepository.findWithCardsById(userId)).thenReturn(Optional.of(user));

    UserWithCardsDto result = userService.findWithCardsById(userId);

    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.name()).isEqualTo("John");
    assertThat(result.surname()).isEqualTo("Doe");
    assertThat(result.email()).isEqualTo("john@example.com");
    assertThat(result.cards()).isNotNull();
  }

  @Test
  void findWithCardsById_whenUserNotFound_shouldThrowUserNotFoundException() {
    Long userId = 1L;

    when(userRepository.findWithCardsById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findWithCardsById(userId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void findByEmail_whenUserExists_shouldReturnUser() {
    String email = "john@example.com";
    User user = Users.buildWithId(1L, "John", "Doe", email);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    UserDto result = userService.findByEmail(email);

    assertThat(result.email()).isEqualTo(email);
    assertThat(result.name()).isEqualTo("John");
    assertThat(result.surname()).isEqualTo("Doe");
  }

  @Test
  void findByEmail_whenUserNotFound_shouldThrowUserNotFoundException() {
    String email = "john@example.com";

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findByEmail(email))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void findAllByIdIn_whenUsersExist_shouldReturnUsersList() {
    List<Long> ids = List.of(1L, 2L);
    List<User> users = List.of(
        Users.buildWithId(1L, "John", "Doe", "john@example.com"),
        Users.buildWithId(2L, "Jane", "Smith", "jane@example.com")
    );

    when(userRepository.findAllByIdIn(ids)).thenReturn(users);

    List<UserDto> result = userService.findAllByIdIn(ids);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).name()).isEqualTo("John");
    assertThat(result.get(1).name()).isEqualTo("Jane");
  }

  @Test
  void findAllByIdInWithCards_whenUsersExist_shouldReturnUsersWithCardsList() {
    List<User> users = List.of(
        Users.build(),
        Users.build()
    );

    List<Long> ids = users.stream().map(User::getId).toList();

    when(userRepository.findAllWithCardsByIdIn(ids))
        .thenReturn(users);

    List<UserWithCardsDto> result = userService.findWithCardsAllByIdIn(ids);

    assertThat(result)
        .containsExactlyInAnyOrderElementsOf(
            users.stream().map(userMapper::toWithCardsDto).toList()
        );
  }
}
