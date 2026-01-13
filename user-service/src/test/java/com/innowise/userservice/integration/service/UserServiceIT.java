package com.innowise.userservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.innowise.common.exception.ResourceAlreadyExistsException;
import com.innowise.common.exception.ResourceNotFoundException;
import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.IT;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@IT
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceIT extends AbstractIntegrationTest {

  private UserDto userDto;

  private final UserService userService;
  private final UserRepository userRepository;
  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  private final UserMapper userMapper;

  private final CacheHelper cacheHelper;

  @BeforeAll
  void prepareFixtures() {
    var userFixture = Users.buildWithoutId();
    Cards.buildWithoutId(userFixture);
    Cards.buildWithoutId(userFixture);
    transactionTemplate.executeWithoutResult(_ ->
        entityManager.persist(userFixture));
    userDto = userMapper.toDto(userFixture);

  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(_ ->
        entityManager.remove(entityManager.find(User.class, userDto.id())));
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
  void findById_whenUserExists_shouldReturnUserResponseDto() {
    assertThat(userService.findById(userDto.id()))
        .isEqualTo(userDto);
  }

  @Test
  void findById_whenInvokedTwice_shouldInvokeOneRepoCallAndCache() {

    userService.findById(userDto.id());
    var cachedUser = userService.findById(userDto.id());

    verify(userRepository, times(1)).findWithCardsById(userDto.id());
    assertThat(cachedUser).isEqualTo(userDto);

  }

  @Test
  @Transactional
  void update_whenCachedValue_shouldUpdateCache() {

    // Invoke caching
    userService.findById(userDto.id());

    var updatedUser = Users.buildWithId(userDto.id());

    userService.update(userDto.id(), UserDto.builder()
        .name(updatedUser.getName())
        .surname(updatedUser.getSurname())
        .birthDate(updatedUser.getBirthDate())
        .email(updatedUser.getEmail())
        .build());

    assertThat(userService.findById(userDto.id()))
        .satisfies(returnedUser -> {
          assertThat(returnedUser)
              .usingRecursiveComparison()
              .ignoringFields("cards")
              .isEqualTo(updatedUser);
          assertThat(returnedUser.cards())
              .containsExactlyElementsOf(userDto.cards());
        });

    verify(userRepository, times(1)).findWithCardsById(userDto.id());

  }

  @Test
  void findById_whenWithoutCardsAndUserNotExists_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userService.findById(Long.MAX_VALUE))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User");
  }

  @Test
  void findByEmail_whenUserExists_shouldReturnUserResponseDtoWithoutCards() {
    assertThat(userService.findByEmail(userDto.email()))
        .isEqualTo(userDto);
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
    assertThat(userService.findAllByIdIn(List.of(userDto.id(), Long.MAX_VALUE)))
        .containsExactlyInAnyOrder(userDto);
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
        .email(userDto.email())
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
    assertThatThrownBy(() -> userService.update(Long.MAX_VALUE, userDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User", "id");
  }

  @Test
  @Transactional
  void update_whenUserExists_shouldReturnUserResponseDto() {
    var newData = Users.build();
    assertThat(userService.update(userDto.id(),
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
  void delete_whenUserExistsAndNotCached_shouldDeleteUser() {
    userService.delete(userDto.id());
    assertThat(entityManager.find(User.class, userDto.id())).isNull();
  }

  @Test
  @Transactional
  void delete_whenUserExistsAndCached_shouldDeleteUser() {

    // Invoke caching
    userService.findById(userDto.id());

    userService.delete(userDto.id());
    assertThat(entityManager.find(User.class, userDto.id())).isNull();
    await()
        .atMost(Duration.ofSeconds(3))
        .untilAsserted(() -> assertThat(cacheHelper.isUserCached(userDto.id())).isFalse());
  }
}
