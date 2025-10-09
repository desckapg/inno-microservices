package com.innowise.authservice.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.innowise.authservice.exception.ResourceAlreadyExistsException;
import com.innowise.authservice.exception.ResourceNotFoundException;
import com.innowise.authservice.integration.AbstractIntegrationTest;
import com.innowise.authservice.integration.annotation.IT;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.entity.Credentials;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.model.mapper.UserMapper;
import com.innowise.authservice.repository.UserRepository;
import com.innowise.authservice.service.UserService;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.datafaker.plugin.DataFakerPlugin;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@IT
@RequiredArgsConstructor
class UserServiceIT extends AbstractIntegrationTest {

  private final UserRepository userRepository;
  private final TestEntityManager em;
  private final UserMapper userMapper;
  private final UserService userService;

  private static final Faker FAKER = new Faker();

  private static final FixtureMonkey SUT = FixtureMonkey.builder()
      .plugin(new DataFakerPlugin())
      .defaultNotNull(true)
      .objectIntrospector(new FailoverIntrospector(
          List.of(
              BuilderArbitraryIntrospector.INSTANCE
          )
      ))
      .register(Credentials.class, fm -> fm.giveMeBuilder(Credentials.class)
          .setLazy("login", () -> FAKER.credentials().username())
          .setLazy("passwordHash", () -> FAKER.credentials().password())
      )
      .register(User.class, fm -> fm.giveMeBuilder(User.class)
          .setNull("id")
          .setNull("userId")
          .size("roles", 1)
          .set("roles[0]", Role.USER)
      )
      .register(CredentialDto.class, fm -> fm.giveMeBuilder(CredentialDto.class)
          .setLazy("login", () -> FAKER.credentials().username())
          .setLazy("password", () -> FAKER.credentials().password())
      )
      .build();

  @Test
  void saveCredentials_whenUserWithThatLoginExists_shouldThrowResourceAlreadyExistsException() {
    var user = SUT.giveMeOne(User.class);
    em.persistAndFlush(user);

    var anotherUser = SUT.giveMeOne(User.class);
    anotherUser.getCredentials().setLogin(user.getCredentials().getLogin());

    var credentials = CredentialDto.builder()
        .login(anotherUser.getCredentials().getLogin())
        .password(anotherUser.getCredentials().getPasswordHash())
        .build();

    assertThatThrownBy(() -> userService.saveCredentials(credentials))
        .isInstanceOf(ResourceAlreadyExistsException.class);
  }

  @Test
  void saveCredentials_whenUserWithThatLoginNotExists_shouldSaveUserCredentials() {
    var credentials = SUT.giveMeOne(CredentialDto.class);

    assertThatNoException().isThrownBy(() ->
        userService.saveCredentials(credentials));
    verify(userRepository, times(1))
        .save(Mockito.any());

    var query = em.getEntityManager()
        .createQuery("SELECT u FROM User u WHERE u.credentials.login = :login");
    query.setParameter("login", credentials.login());
    assertThat(query.getSingleResult()).isNotNull();
  }

  @Test
  void deleteCredentials_whenUserNotExists_shouldThrowResourceNotFoundException() {
    assertThatThrownBy(() -> userService.deleteCredentials(1L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void deleteCredentials_whenUserExists_shouldDeleteCredentials() {
    var user = SUT.giveMeOne(User.class);
    var id = em.persistAndGetId(user, Long.class);

    assertThat(id).isNotNull();
    assertThatNoException().isThrownBy(() ->
        userService.deleteCredentials(id));
    verify(userRepository, times(1)).deleteById(id);

    assertThat(em.find(User.class, id)).isNull();
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
