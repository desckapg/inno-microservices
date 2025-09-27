package com.innowise.userservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.exception.ResourceAlreadyExistsException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.ServiceIT;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.UserServiceImpl;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@ServiceIT
@RequiredArgsConstructor
class UserServiceIT extends AbstractIntegrationTest {

  private User userFixture;

  @MockitoSpyBean
  private final UserRepository userRepository;

  @InjectMocks
  private final UserServiceImpl userService;

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  private final UserMapper userMapper;

  private final CacheHelper cacheHelper;

  @BeforeAll
  void prepareFixtures() {
    userFixture = Users.buildWithoutId();
    Cards.buildWithoutId(userFixture);
    Cards.buildWithoutId(userFixture);
    transactionTemplate.executeWithoutResult(status -> entityManager.persist(userFixture));
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(status ->
        entityManager.remove(entityManager.find(User.class, userFixture.getId())));
  }

  @AfterEach
  void cleanupCache() {
    cacheHelper.invalidate();
  }

  @Test
  void contextLoads() {
    assertThat(userService).isNotNull();
  }

  @Test
  void findById_whenUserExists_shouldReturnUserResponseDtoWithoutCards() {
    assertThat(userService.findById(userFixture.getId()))
        .isEqualTo(userMapper.toDto(userFixture));
  }

  @Test
  void findById_whenInvokedTwice_shouldExecuteOneSqlStatement() {

    userService.findById(userFixture.getId());
    var cachedUser = userService.findById(userFixture.getId());

    verify(userRepository, times(1)).findWithCardsById(userFixture.getId());
    assertThat(cachedUser).isEqualTo(userMapper.toDto(userFixture));

  }

  @Test
  void findById_whenWithoutCardsAndUserNotExists_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userService.findById(Long.MAX_VALUE))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User");
  }

  @Test
  void findByEmail_whenUserExists_shouldReturnUserResponseDtoWithoutCards() {
    assertThat(userService.findByEmail(userFixture.getEmail()))
        .isEqualTo(userMapper.toDto(userFixture));
  }

  @Test
  void findByEmail_whenUserNotExists_shouldThrowUserNotFoundException() {
    assertThatThrownBy(
        () -> userService.findByEmail("nonexisting@example.com")).isInstanceOf(
            ResourceNotFoundException.class)
        .hasMessageContaining("User", "email");
  }

  @Test
  void findAllByIdIn_whenUsersExist_shouldReturnListOfUserResponseDto() {
    assertThat(userService.findAllByIdIn(List.of(userFixture.getId(), Long.MAX_VALUE)))
        .containsExactly(userMapper.toDto(userFixture));
  }

  @Test
  void findAllByIdIn_whenUsersNotExist_shouldReturnEmptyList() {
    assertThat(userService.findAllByIdIn(List.of(Long.MAX_VALUE)))
        .isEmpty();
  }

  @Test
  void findWithCardsAllByIdIn_whenUsersNotExist_shouldReturnEmptyList() {
    assertThat(userService.findAllByIdIn(List.of(Long.MAX_VALUE)))
        .isEmpty();
  }

  @Test
  @Transactional
  void create_whenUserWithEmailExists_shouldThrowUserWithEmailExistsException() {
    var creatingdUser = Users.buildWithoutId();
    var createDto = UserDto.builder()
        .name(creatingdUser.getName())
        .surname(creatingdUser.getSurname())
        .birthDate(creatingdUser.getBirthDate())
        .email(userFixture.getEmail())
        .build();

    assertThatThrownBy(() -> userService.create(createDto))
        .isInstanceOf(ResourceAlreadyExistsException.class)
        .hasMessageContaining("User", "id");
  }

  @Test
  @Transactional
  void create_whenUserWithEmailNotExists_shouldReturnCreatedUserResponseDto() {
    var newUser = Users.build();
    var createDto = UserDto.builder()
        .name(newUser.getName())
        .surname(newUser.getSurname())
        .birthDate(newUser.getBirthDate())
        .email(newUser.getEmail())
        .build();

    assertThat(userService.create(createDto))
        .usingRecursiveComparison()
        .ignoringFields("id", "cards")
        .isEqualTo(newUser);
  }

  @Test
  @Transactional
  void update_whenUserNotExist_shouldThrowUserNotFoundException() {
    var updateDto = UserDto.builder()
        .name(userFixture.getName())
        .surname(userFixture.getSurname())
        .birthDate(userFixture.getBirthDate())
        .email(userFixture.getEmail())
        .build();

    assertThatThrownBy(() -> userService.update(Long.MAX_VALUE, updateDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User", "id");
  }

  @Test
  @Transactional
  void update_whenUserExists_shouldReturnUserResponseDto() {
    var newData = Users.build();
    assertThat(userService.update(userFixture.getId(),
        UserDto.builder()
            .name(newData.getName())
            .surname(newData.getSurname())
            .email(newData.getEmail())
            .birthDate(newData.getBirthDate())
            .build())
    )
        .usingRecursiveComparison()
        .ignoringFields("id", "cards")
        .isEqualTo(userMapper.toDto(newData));
  }

  @Test
  @Transactional
  void delete_whenUserNotExist_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userService.delete(Long.MAX_VALUE))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User", "id");
  }

  @Test
  @Transactional
  void delete_whenUserExists_shouldDeleteUser() {
    userService.delete(userFixture.getId());
    assertThat(entityManager.find(User.class, userFixture.getId())).isNull();
  }
}
