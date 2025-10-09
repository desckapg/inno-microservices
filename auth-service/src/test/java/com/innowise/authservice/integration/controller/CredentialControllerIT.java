package com.innowise.authservice.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.authservice.integration.AbstractIntegrationTest;
import com.innowise.authservice.integration.annotation.IT;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.entity.Credentials;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.datafaker.plugin.DataFakerPlugin;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@IT
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CredentialControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/auth/credentials";

  private static final Faker FAKER = new Faker();

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final TestEntityManager em;
  private final PasswordEncoder passwordEncoder;

  private FixtureMonkey sut;

  @BeforeAll
  void prepareFixtureMonkey() {
    sut = FixtureMonkey.builder()
        .plugin(new DataFakerPlugin())
        .defaultNotNull(true)
        .objectIntrospector(new FailoverIntrospector(
            List.of(
                BuilderArbitraryIntrospector.INSTANCE
            )
        ))
        .register(Credentials.class, fm -> fm.giveMeBuilder(Credentials.class)
            .setLazy("login", () -> FAKER.credentials().username())
            .setLazy("passwordHash", () -> passwordEncoder.encode(FAKER.credentials().password()))
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
  }

  private static Stream<Arguments> invalidCredentialDtos() {
    return Stream.of(
        Arguments.of(
            CredentialDto.builder()
                .login("ab")
                .password("Password1")
                .build(),
            "login",
            "Login length must be between 3 and 24"
        ),
        Arguments.of(
            CredentialDto.builder()
                .password("Password1")
                .build(),
            "login",
            "Login must be provided"
        ),
        Arguments.of(
            CredentialDto.builder()
                .login("veryyyyyyyyyyyyyyyyyyyyyylongloginvalueX")
                .password("Password1")
                .build(),
            "login",
            "Login length must be between 3 and 24"
        ),
        Arguments.of(
            CredentialDto.builder()
                .login("user1")
                .build(),
            "password",
            "Password must be provided"
        ),
        Arguments.of(
            CredentialDto.builder()
                .login("user1")
                .password("short")
                .build(),
            "password",
            "Password must contain at least 8 characters, one digit and one letter"
        ),
        Arguments.of(
            CredentialDto.builder()
                .login("user1")
                .password("passwordwithoutdigit")
                .build(),
            "password",
            "Password must contain at least 8 characters, one digit and one letter"
        )
    );
  }

  @ParameterizedTest(name = "{index}: {2}")
  @MethodSource("invalidCredentialDtos")
  void save_invalidCredentials_returnUnprocessable(CredentialDto credentials, String field,
      String expectedError)
      throws Exception {

    String body = objectMapper.writeValueAsString(credentials);
    mockMvc.perform(post(BASE_URL)
            .contentType("application/json")
            .content(body))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.field").value(field))
        .andExpect(jsonPath("$.fieldViolation").value(expectedError));
  }

  @Test
  void save_validCredentialsButUserAlsoExists_returnConflict() throws Exception {

    var user = sut.giveMeOne(User.class);
    em.persistAndFlush(user);
    em.clear();

    CredentialDto credentialDto = CredentialDto.builder()
        .login(user.getCredentials().getLogin())
        .password(FAKER.credentials().password())
        .build();

    String body = objectMapper.writeValueAsString(credentialDto);
    mockMvc.perform(post(BASE_URL)
            .contentType("application/json")
            .content(body))
        .andExpect(status().isConflict());

  }

  @Test
  void save_validCredentialsAndUserNotExists_returnCreatedAndUserDto() throws Exception {

    var password = FAKER.credentials().password();

    var credentials = Credentials.builder()
        .login(FAKER.credentials().username())
        .passwordHash(passwordEncoder.encode(password))
        .build();

    CredentialDto credentialDto = CredentialDto.builder()
        .login(credentials.getLogin())
        .password(password)
        .build();

    String body = objectMapper.writeValueAsString(credentialDto);

    mockMvc.perform(post(BASE_URL)
            .contentType("application/json")
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.login").value(credentials.getLogin()));
  }

  @Test
  void delete_userNotExists_returnNotFound() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/{id}", 1L)
            .contentType("application/json"))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_userExists_returnNoContent() throws Exception {

    var user = sut.giveMeOne(User.class);
    var id = em.persistAndGetId(user);
    em.flush();
    em.clear();

    mockMvc.perform(delete(BASE_URL + "/{id}", id)
            .contentType("application/json"))
        .andExpect(status().isNoContent());
  }

}
