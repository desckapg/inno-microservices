package com.innowise.authservice.serivce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.innowise.authservice.exception.AuthFailedException;
import com.innowise.authservice.exception.ResourceNotFoundException;
import com.innowise.authservice.exception.TokenException;
import com.innowise.authservice.exception.TokenException.TokenErrorCode;
import com.innowise.authservice.model.dto.credential.CredentialDto;
import com.innowise.authservice.model.dto.user.UserDto;
import com.innowise.authservice.service.impl.TokenServiceImpl;
import com.innowise.authservice.service.impl.UserServiceImpl;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import java.lang.reflect.Method;
import java.util.List;
import lombok.SneakyThrows;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.arbitraries.LongArbitrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({SystemStubsExtension.class, MockitoExtension.class})
class TokenServiceTest {

  private static final String TEST_ACCESS_KEY = "6e4d3f160f46d03de0d5f3ac52d2e19797cbbe9ff71ab1c168ce31bb5d4df87e";
  private static final String TEST_REFRESH_KEY = "b1819099b124e415850b817aba0b2b42ade9ac3167cc2b8d85aca419d728cf3a";

  private static final String TEST_ANOTHER_REFRESH_KEY = "679819a27a16b01280cb8e0d74321a13b217ef79a9c2deb3788ff961874b875f";
  private static final String TEST_ANOTHER_ACCESS_KEY = "b659a318b18ab27da3dbbc2a0a345cdc59d01f1f21b8397fc2f105894851bc1e";

  private static final int TEST_ACCESS_EXPIRATION = 900;
  private static final int TEST_REFRESH_EXPIRATION = 604800;

  private static final FixtureMonkey sut = FixtureMonkey.builder()
      .plugin(new JqwikPlugin().javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
        @Override
        public LongArbitrary longs() {
          return Arbitraries.longs().greaterOrEqual(1);
        }
      })).objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .defaultNotNull(true).nullableContainer(false).nullableElement(false)
      .defaultNullInjectGenerator(_ -> 0.0).build();

  @SystemStub
  private final EnvironmentVariables envVariables = new EnvironmentVariables();

  @Mock
  private UserServiceImpl userService;

  @InjectMocks
  private TokenServiceImpl tokenService;

  @BeforeEach
  void prepareTokenService() {
    envVariables.set("JWT_ACCESS_KEY", TEST_ACCESS_KEY);
    envVariables.set("JWT_REFRESH_KEY", TEST_REFRESH_KEY);
    envVariables.set("JWT_EXPIRATION", String.valueOf(TEST_ACCESS_EXPIRATION));
    envVariables.set("JWT_REFRESH_EXPIRATION", String.valueOf(TEST_REFRESH_EXPIRATION));
  }

  @SneakyThrows
  private String createAccessToken(Long userAuthId, Long userProfileId, List<String> roles) {
    Method method = tokenService.getClass()
        .getDeclaredMethod("createAccessToken", Long.class, Long.class, List.class);
    method.setAccessible(true);
    return (String) method.invoke(tokenService, userAuthId, userProfileId, roles);
  }

  @SneakyThrows
  private String createRefreshToken(Long userAuthId) {
    Method method = tokenService.getClass().getDeclaredMethod("createRefreshToken", Long.class);
    method.setAccessible(true);
    return (String) method.invoke(tokenService, userAuthId);
  }

  @Test
  void createTokens_whenUserValid_shouldReturnPairsOfTokens() {
    var credentialDto = sut.giveMeOne(CredentialDto.class);
    var userDto = sut.giveMeBuilder(UserDto.class).set("login", credentialDto.login())
        .set("password", credentialDto.password()).sample();

    when(userService.findByLoginAndPassword(credentialDto.login(),
        credentialDto.password())).thenReturn(userDto);

    var tokenDto = tokenService.createTokens(credentialDto);

    assertThat(tokenDto).isNotNull();
    assertThat(tokenDto.accessToken()).isNotNull();
    assertThat(tokenDto.refreshToken()).isNotNull();

    assertThatNoException().isThrownBy(
        () -> JWT.require(Algorithm.HMAC256(System.getenv("JWT_ACCESS_KEY"))).build()
            .verify(tokenDto.accessToken()));
    assertThatNoException().isThrownBy(
        () -> JWT.require(Algorithm.HMAC256(System.getenv("JWT_REFRESH_KEY"))).build()
            .verify(tokenDto.refreshToken()));
  }

  @Test
  void createTokens_whenUserInvalid_shouldReturnPairsOfTokens() {
    var credentialDto = sut.giveMeOne(CredentialDto.class);

    when(userService.findByLoginAndPassword(credentialDto.login(),
        credentialDto.password())).thenThrow(new AuthFailedException());

    assertThatThrownBy(() -> tokenService.createTokens(credentialDto)).isInstanceOf(
        AuthFailedException.class);

  }

  @Test
  void refreshAccessToken_whenTokenSignedByAnotherKey_shouldThrowTokenException() {
    var userDto = sut.giveMeOne(UserDto.class);

    String refreshToken = createRefreshToken(userDto.id());

    envVariables.set("JWT_REFRESH_KEY", TEST_ANOTHER_REFRESH_KEY);
    assertThatException().isThrownBy(() -> tokenService.refreshAccessToken(refreshToken))
        .isInstanceOf(TokenException.class);
  }

  @Test
  void refreshAccessToken_whenTokenValidAndUserNotExists_shouldThrowAuthFailedException() {
    var userDto = sut.giveMeOne(UserDto.class);

    String refreshToken = createRefreshToken(userDto.id());

    when(userService.findById(userDto.id())).thenThrow(
        sut.giveMeOne(ResourceNotFoundException.class));

    assertThatException().isThrownBy(() -> tokenService.refreshAccessToken(refreshToken))
        .isInstanceOf(AuthFailedException.class);
  }

  @Test
  void refreshAccessToken_whenTokenExpired_shouldThrowTokenException() {
    var userDto = sut.giveMeOne(UserDto.class);

    envVariables.set("JWT_REFRESH_EXPIRATION", 0);

    String refreshToken = createRefreshToken(userDto.id());

    assertThatException().isThrownBy(() -> tokenService.refreshAccessToken(refreshToken))
        .isInstanceOf(TokenException.class);
  }

  @Test
  void refreshAccessToken_whenTokenValidAndUserExists_shouldReturnNewAccessToken() {
    var userDto = sut.giveMeOne(UserDto.class);

    String refreshToken = createRefreshToken(userDto.id());

    when(userService.findById(userDto.id())).thenReturn(userDto);

    var tokenDto = tokenService.refreshAccessToken(refreshToken);

    assertThat(tokenDto).isNotNull();
    assertThat(tokenDto.accessToken()).isNotNull();
    assertThat(tokenDto.refreshToken()).isNull();
    assertThatNoException().isThrownBy(
        () -> JWT.require(Algorithm.HMAC256(System.getenv("JWT_ACCESS_KEY"))).build()
            .verify(tokenDto.accessToken()));
  }

  @Test
  void validateAccessToken_whenTokenValid_shouldThrowNoExceptions() {

    var userDto = sut.giveMeOne(UserDto.class);
    String accessToken = createAccessToken(userDto.id(), userDto.userId(), userDto.roles());

    assertThatNoException().isThrownBy(() -> tokenService.validateAccessToken(accessToken));
  }

  @Test
  void validateAccessToken_whenTokenSignedByAnotherKey_shouldThrowNoExceptions() {

    var userDto = sut.giveMeOne(UserDto.class);

    envVariables.set("JWT_ACCESS_KEY", TEST_ANOTHER_ACCESS_KEY);
    String accessToken = createAccessToken(userDto.id(), userDto.userId(), userDto.roles());

    envVariables.set("JWT_ACCESS_KEY", TEST_ACCESS_KEY);
    assertThatException().isThrownBy(() -> tokenService.validateAccessToken(accessToken))
        .isInstanceOfSatisfying(TokenException.class,
            ex -> assertThat(ex.getErrorCode()).isEqualTo(TokenErrorCode.INVALID_SIGNATURE));
  }

  @Test
  void validateAccessToken_whenTokenExpired_shouldThrowNoExceptions() {

    var userDto = sut.giveMeOne(UserDto.class);

    envVariables.set("JWT_EXPIRATION", 0);

    String accessToken = createAccessToken(userDto.id(), userDto.userId(), userDto.roles());

    assertThatException().isThrownBy(() -> tokenService.validateAccessToken(accessToken))
        .isInstanceOfSatisfying(TokenException.class,
            ex -> assertThat(ex.getErrorCode()).isEqualTo(TokenErrorCode.EXPIRED));

  }

}
