package com.innowise.authservice.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.innowise.authservice.integration.AbstractIntegrationTest;
import com.innowise.authservice.integration.annotation.IT;
import com.innowise.authservice.model.dto.UserConstraints;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.user.UserAuthDto;
import com.innowise.authservice.model.dto.user.UserAuthInfoDto;
import com.innowise.authservice.model.dto.user.UserInfoDto;
import com.innowise.authservice.model.entity.Credentials;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.service.TokenService;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.datafaker.plugin.DataFakerPlugin;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@IT
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/auth";

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final TestEntityManager em;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

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
            .setNull("id")
            .size("roles", 1)
            .set("roles[0]", Role.USER)
        )
        .register(CredentialDto.class, fm -> fm.giveMeBuilder(CredentialDto.class)
            .setLazy("login", () -> FAKER.credentials().username())
            .setLazy("password", () -> FAKER.credentials().password())
        )
        .register(UserAuthDto.class, fm -> fm.giveMeBuilder(UserAuthDto.class)
            .setNull("id")
            .set("userId", 1L)
        )
        .register(UserInfoDto.class, fm -> fm.giveMeBuilder(UserInfoDto.class)
            .setNull("id")
            .setLazy("name", () -> FAKER.name().firstName())
            .setLazy("surname", () -> FAKER.name().lastName())
            .set("birthDate", LocalDate.of(1970, 12, 1))
            .setLazy("email", () -> FAKER.internet().emailAddress())
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
            "authDto.credentials.login",
            "Login length must be between 3 and 24"
        ),
        Arguments.of(
            CredentialDto.builder()
                .password("Password1")
                .build(),
            "authDto.credentials.login",
            "Login must be provided"
        ),
        Arguments.of(
            CredentialDto.builder()
                .login("veryyyyyyyyyyyyyyyyyyyyyyylongloginvalueX")
                .password("Password1")
                .build(),
            "authDto.credentials.login",
            "Login length must be between 3 and 24"
        ),
        Arguments.of(
            CredentialDto.builder()
                .login("user1")
                .build(),
            "authDto.credentials.password",
            "Password must be provided"
        ),
        Arguments.of(
            CredentialDto.builder()
                .login("user1")
                .password("short")
                .build(),
            "authDto.credentials.password",
            "Password must contain at least 8 characters, one digit and one letter"
        ),
        Arguments.of(
            CredentialDto.builder()
                .login("user1")
                .password("passwordwithoutdigit")
                .build(),
            "authDto.credentials.password",
            "Password must contain at least 8 characters, one digit and one letter"
        )
    );
  }

  @ParameterizedTest(name = "{index}: {2}")
  @MethodSource("invalidCredentialDtos")
  void register_invalidCredentials_returnUnprocessableStatus(CredentialDto credentials,
      String field,
      String expectedError)
      throws Exception {

    var userAuthInfoDto = sut.giveMeBuilder(UserAuthInfoDto.class)
        .set("authDto.credentials", credentials)
        .sample();

    var createdUserInfoDto = UserInfoDto.builder()
        .id(1L)
        .name(userAuthInfoDto.infoDto().name())
        .surname(userAuthInfoDto.infoDto().surname())
        .birthDate(userAuthInfoDto.infoDto().birthDate())
        .email(userAuthInfoDto.infoDto().email())
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

    userServiceClientServer.stubFor(
        WireMock.delete("/api/v1/users/" + createdUserInfoDto.id())
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NO_CONTENT.value())
            )
    );

    String body = objectMapper.writerWithView(UserConstraints.Register.class)
        .writeValueAsString(userAuthInfoDto);

    mockMvc.perform(post(BASE_URL + "/register")
            .contentType("application/json")
            .content(body))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.field").value(field))
        .andExpect(jsonPath("$.fieldViolation").value(expectedError));
  }

  @Test
  void login_userNotExists_returnUnauthorizedStatus() throws Exception {

    var user = sut.giveMeOne(User.class);
    em.persistAndFlush(user);
    em.clear();

    CredentialDto credentialDto = CredentialDto.builder()
        .login(user.getCredentials().getLogin())
        .password(FAKER.credentials().password())
        .build();

    String body = objectMapper.writeValueAsString(credentialDto);
    mockMvc.perform(post(BASE_URL + "/login")
            .contentType("application/json")
            .content(body))
        .andExpect(status().isUnauthorized());

  }

  @Test
  void login_validCredentialsAndUserExists_returnTokens() throws Exception {

    var password = FAKER.credentials().password();

    var credentials = Credentials.builder()
        .login(FAKER.credentials().username())
        .passwordHash(passwordEncoder.encode(password))
        .build();

    var user = sut.giveMeBuilder(User.class)
        .set("credentials", credentials)
        .sample();
    em.persistAndFlush(user);
    em.clear();

    CredentialDto credentialDto = CredentialDto.builder()
        .login(credentials.getLogin())
        .password(password)
        .build();

    String body = objectMapper.writeValueAsString(credentialDto);
    mockMvc.perform(post(BASE_URL + "/login")
            .contentType("application/json")
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }

  @Test
  void refresh_refreshTokenNotProvided_returnBadRequest() throws Exception {
    mockMvc.perform(post(BASE_URL + "/refresh"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void refresh_refreshTokenProvided_returnBadRequest() throws Exception {

    var password = FAKER.credentials().password();

    var credentials = Credentials.builder()
        .login(FAKER.credentials().username())
        .passwordHash(passwordEncoder.encode(password))
        .build();

    var user = sut.giveMeBuilder(User.class)
        .set("credentials", credentials)
        .sample();
    em.persistAndFlush(user);
    em.clear();

    CredentialDto credentialDto = CredentialDto.builder()
        .login(credentials.getLogin())
        .password(password)
        .build();

    var tokens = tokenService.createTokens(credentialDto);

    mockMvc.perform(post(BASE_URL + "/refresh")
            .header("X-Refresh-Token", tokens.refreshToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());
  }

  @Test
  void validate_accessNotProvided_returnBadRequest() throws Exception {
    mockMvc.perform(get(BASE_URL + "/validate"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void validate_accessTokenProvidedAndValid_returnOk() throws Exception {

    var password = FAKER.credentials().password();

    var credentials = Credentials.builder()
        .login(FAKER.credentials().username())
        .passwordHash(passwordEncoder.encode(password))
        .build();

    var user = sut.giveMeBuilder(User.class)
        .set("credentials", credentials)
        .sample();
    em.persistAndFlush(user);
    em.clear();

    CredentialDto credentialDto = CredentialDto.builder()
        .login(credentials.getLogin())
        .password(password)
        .build();

    var tokens = tokenService.createTokens(credentialDto);

    mockMvc.perform(get(BASE_URL + "/validate")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
        .andExpect(status().isOk());
  }

}
