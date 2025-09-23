package com.innowise.userservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.ServiceIT;
import com.innowise.userservice.model.dto.user.UserCreateRequestDto;
import com.innowise.userservice.model.dto.user.UserUpdateRequestDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.UserMapper;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@ServiceIT
@RequiredArgsConstructor
public class UserServiceIT extends AbstractIntegrationTest {

  private User userFixture;

  private final UserService userService;

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  private final UserMapper userMapper;

  @BeforeAll
  void prepareFixtures() {
    userFixture = Users.buildWithoutId();
    Cards.buildWithoutId(userFixture);
    Cards.buildWithoutId(userFixture);
    transactionTemplate.executeWithoutResult(status -> {
      entityManager.persist(userFixture);
    });
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(status -> {
      entityManager.remove(entityManager.find(User.class, userFixture.getId()));
    });
  }

  @Test
  void contextLoads() {
    assertThat(userService).isNotNull();
  }

  @Test
  void findById_whenWithoutCardsAndUserExists_shouldReturnUserResponseDtoWithoutCards() {
    assertThat(userService.findWithoutCardsById(userFixture.getId())).isEqualTo(
        userMapper.toDto(userFixture));
  }

  @Test
  void findById_whenWithCardsAndUserExists_shouldReturnUserResponseDtoWithCards() {
    assertThat(userService.findWithCardsById(userFixture.getId())).isEqualTo(
        userMapper.toWithCardsDto(userFixture));
  }

  @Test
  void findById_whenWithoutCardsAndUserNotExists_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userService.findWithoutCardsById(Long.MAX_VALUE)).isInstanceOf(
        UserNotFoundException.class);
  }

  @Test
  void findById_whenWithCardsAndUserNotExists_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userService.findWithCardsById(Long.MAX_VALUE)).isInstanceOf(
        UserNotFoundException.class);
  }

  @Test
  void findByEmail_whenWithoutCardsAndUserExists_shouldReturnUserResponseDtoWithoutCards() {
    assertThat(userService.findByEmail(userFixture.getEmail(), false)).isEqualTo(
        userMapper.toDto(userFixture));
  }

  @Test
  void findByEmail_whenWithCardsAndUserExists_shouldReturnUserResponseDtoWithCards() {
    assertThat(userService.findByEmail(userFixture.getEmail(), true)).isEqualTo(
        userMapper.toWithCardsDto(userFixture));
  }

  @Test
  void findByEmail_whenWithoutCardsAndUserNotExists_shouldThrowUserNotFoundException() {
    assertThatThrownBy(
        () -> userService.findByEmail("nonexisting@example.com", false)).isInstanceOf(
        UserNotFoundException.class);
  }

  @Test
  void findByEmail_whenWithCardsAndUserNotExists_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userService.findByEmail("nonexisting@example.com", true)).isInstanceOf(
        UserNotFoundException.class);
  }

  @Test
  void findAllByIdIn_whenWithoutCardsAndUsersExist_shouldReturnListOfUserResponseDtoWithoutCards() {
    assertThat(userService.findAllByIdIn(List.of(userFixture.getId(), Long.MAX_VALUE),
        false)).containsExactly(userMapper.toDto(userFixture));
  }

  @Test
  void findAllByIdIn_whenWithCardsAndUsersExist_shouldReturnListOfUserResponseDtoWithCards() {
    assertThat(userService.findAllByIdIn(List.of(userFixture.getId(), Long.MAX_VALUE),
        true)).containsExactly(userMapper.toWithCardsDto(userFixture));
  }

  @Test
  void findAllByIdIn_whenWithoutCardsAndUsersNotExist_shouldReturnEmptyList() {
    assertThat(userService.findAllByIdIn(List.of(Long.MAX_VALUE), false)).isEmpty();
  }

  @Test
  void findAllByIdIn_whenWithCardsAndUsersNotExist_shouldReturnEmptyList() {
    assertThat(userService.findAllByIdIn(List.of(Long.MAX_VALUE), true)).isEmpty();
  }

  @Test
  @Transactional
  void create_whenUserWithEmailExists_shouldThrowUserWithEmailExistsException() {
    assertThatThrownBy(() -> userService.create(
        new UserCreateRequestDto("New", "User", userFixture.getBirthDate(),
            userFixture.getEmail()))).isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  @Transactional
  void create_whenUserWithEmailNotExists_shouldReturnCreatedUserResponseDto() {
    var newUser = Users.build();
    var createDto = new UserCreateRequestDto(newUser.getName(), newUser.getSurname(),
        newUser.getBirthDate(), newUser.getEmail());
    assertThat(userService.create(createDto)).usingRecursiveComparison().ignoringFields("id")
        .isEqualTo(userMapper.toDto(newUser));
  }

  @Test
  @Transactional
  void update_whenUserNotExist_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userService.update(Long.MAX_VALUE,
        new UserUpdateRequestDto(userFixture.getName(), userFixture.getSurname(),
            userFixture.getEmail(), userFixture.getBirthDate()))).isInstanceOf(
        UserNotFoundException.class);
  }

  @Test
  @Transactional
  void update_whenUserExists_shouldReturnUserResponseDto() {
    var newData = Users.build();
    assertThat(userService.update(userFixture.getId(),
        new UserUpdateRequestDto(
            newData.getName(),
            newData.getSurname(),
            newData.getEmail(),
            newData.getBirthDate()))
    )
        .usingRecursiveComparison()
        .ignoringFields("id", "cards")
        .isEqualTo(userMapper.toDto(newData));
  }

  @Test
  @Transactional
  void delete_whenUserNotExist_shouldThrowUserNotFoundException() {
    assertThatThrownBy(() -> userService.delete(Long.MAX_VALUE)).isInstanceOf(
        UserNotFoundException.class);
  }

  @Test
  @Transactional
  void delete_whenUserExists_shouldDeleteUser() {
    userService.delete(userFixture.getId());
    assertThat(entityManager.find(User.class, userFixture.getId())).isNull();
  }
}
