package com.innowise.authservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.innowise.authservice.integration.AbstractIntegrationTest;
import com.innowise.authservice.integration.annotation.IT;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.user.UserAuthDto;
import com.innowise.authservice.model.dto.user.UserProfileDto;
import com.innowise.authservice.model.dto.user.UserRegisterRequestDto;
import com.innowise.authservice.model.entity.Credentials;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.model.mapper.UserMapper;
import com.innowise.authservice.repository.UserRepository;
import com.innowise.authservice.service.UserService;
import com.innowise.common.exception.ResourceNotFoundException;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

@IT
@RequiredArgsConstructor
@TestInstance(Lifecycle.PER_CLASS)
class UserServiceIT extends AbstractIntegrationTest {

  private final TestEntityManager em;

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  private static final FixtureMonkey SUT = FixtureMonkey.builder()
      .defaultNotNull(true)
      .objectIntrospector(new FailoverIntrospector(
          List.of(
            BuilderArbitraryIntrospector.INSTANCE,
            ConstructorPropertiesArbitraryIntrospector.INSTANCE
          )
      ))
      .register(CredentialDto.class, fm -> fm.giveMeBuilder(CredentialDto.class)
        .setLazy("login", () -> FAKER.credentials().username())
        .setLazy("password", () -> FAKER.credentials().password())
      )
      .register(UserAuthDto.class, fm -> fm.giveMeBuilder(UserAuthDto.class)
          .setNull("id")
          .set("userId", 1L)
      )
      .register(UserProfileDto.class, fm -> fm.giveMeBuilder(UserProfileDto.class)
          .setNull("id")
          .setLazy("name", () -> FAKER.name().firstName())
          .setLazy("surname", () -> FAKER.name().lastName())
          .set("birthDate", LocalDate.of(1970, 12, 1))
          .setLazy("email", () -> FAKER.internet().emailAddress())
      )
      .register(Credentials.class, fm -> fm.giveMeBuilder(Credentials.class)
          .setLazy("login", () -> FAKER.credentials().username())
          .setLazy("passwordHash", () -> FAKER.credentials().password())
      )
      .register(UserRegisterRequestDto.class, fm -> fm.giveMeBuilder(UserRegisterRequestDto.class)
          .setLazy("login", () -> FAKER.credentials().username())
          .setLazy("password", () -> FAKER.credentials().password())
          .setLazy("name", () -> FAKER.name().firstName())
          .setLazy("surname", () -> FAKER.name().lastName())
          .set("birthDate", LocalDate.of(1970, 12, 1))
          .setLazy("email", () -> FAKER.internet().emailAddress())
      )
      .register(User.class, fm -> fm.giveMeBuilder(User.class)
          .setNull("id")
          .size("roles", 1)
          .set("roles[0]", Role.USER)
      )
      .build();

  @Test
  void register_createNewUser() {
    var userRegisterDto = SUT.giveMeOne(UserRegisterRequestDto.class);

    var createdUserInfoDto = UserProfileDto.builder()
        .id(1L)
        .name(userRegisterDto.name())
        .surname(userRegisterDto.surname())
        .birthDate(userRegisterDto.birthDate())
        .email(userRegisterDto.email())
        .build();

    userServiceClientServer.stubFor(
        WireMock.post("/api/v1/users")
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.CREATED.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withResponseBody(Body.fromJsonBytes(
                    objectMapper.writeValueAsBytes(createdUserInfoDto))
                )
            )
    );

    assertThat(userService.register(userRegisterDto)).satisfies(createdUserAuthInfoDto -> {

      assertThat(createdUserAuthInfoDto).isNotNull();

      assertThat(createdUserAuthInfoDto.id()).isNotNull();
      assertThat(createdUserAuthInfoDto)
          .usingRecursiveComparison()
          .ignoringFields("id", "roles", "credentials.password")
          .isEqualTo(userRegisterDto);
    });

  }

  @Test
  void register_exceptionArisen_rollbackCreation() {
    var userRegisterDto = SUT.giveMeOne(UserRegisterRequestDto.class);

    doThrow(new RuntimeException())
        .when(userRepository)
        .save(any(User.class));

    assertThatException().isThrownBy(() -> userService.register(userRegisterDto));
  }

  @Test
  void register_userWithThatLoginExists_rollbackCreation() {
    var userRegisterDto = SUT.giveMeOne(UserRegisterRequestDto.class);

    when(userRepository.existsByLogin(anyString())).thenReturn(true);

    doThrow(new RuntimeException())
        .when(userRepository)
        .save(any(User.class));

    assertThatException().isThrownBy(() -> userService.register(userRegisterDto));

  }

  @Test
  void delete_userNotExists_deleteNothing() {
    assertThatNoException().isThrownBy(() -> userService.delete(Long.MAX_VALUE));
  }

  @Test
  void delete_userExists_deleteUser() {
    var user = SUT.giveMeBuilder(User.class).sample();

    em.persistAndFlush(user);

    userServiceClientServer.stubFor(
        WireMock.delete("/api/v1/users/" + user.getId())
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NO_CONTENT.value())
            )
    );

    assertThatNoException().isThrownBy(() -> userService.delete(user.getId()));

    assertThat(em.find(User.class, user.getId())).isNull();
  }

  @Test
  void findByLogin_userNotExists_throwResourceNotFoundException() {
    var login = FAKER.credentials().username();
    assertThatThrownBy(() -> userService.findByLogin(login))
        .isInstanceOf(ResourceNotFoundException.class);
  }


  @Test
  void findByLogin_userExists_returnUserDto() {
    var user = SUT.giveMeOne(User.class);
    em.persistAndFlush(user);
    em.clear();

    assertThat(userService.findByLogin(user.getCredentials().getLogin()))
        .isEqualTo(userMapper.toDto(user));
  }

}
