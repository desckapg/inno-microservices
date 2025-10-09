package com.innowise.authservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Version;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategy;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class CustomBCryptPasswordEncoderTest {

  private static final BCrypt.Version VERSION = Version.VERSION_2Y;
  private static final LongPasswordStrategy LONG_PASSWORD_STRATEGY =
      LongPasswordStrategies.hashSha512(VERSION);
  private static final int ITERATIONS = 12;

  private static final Faker FAKER = new Faker();

  private final CustomBCryptPasswordEncoder passwordEncoder = new CustomBCryptPasswordEncoder();

  @Test
  void encode_rawPasswordIsNull_throwIllegalArgumentException() {
    assertThatThrownBy(() -> passwordEncoder.encode(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void encode_rawPasswordNotNull_returnHashedPassword() {
    assertThat(passwordEncoder.encode(FAKER.credentials().password()))
        .satisfies(password -> {
          assertThat(password).isNotNull();
          assertThat(password).isNotEmpty();
          assertThat(password).isNotBlank();
        });
  }

  @Test
  void matches_rawPasswordIsNull_throwIllegalArgumentException() {
    var encodedPassword = new String(BCrypt
        .with(VERSION, LONG_PASSWORD_STRATEGY)
        .hash(ITERATIONS, FAKER.credentials().password().getBytes())
    );
    assertThatThrownBy(() -> passwordEncoder.matches(null, encodedPassword))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void matches_encodedPasswordIsNull_throwIllegalArgumentException() {
    var rawPassword = FAKER.credentials().password();
    assertThatThrownBy(() -> passwordEncoder.matches(rawPassword, null))
        .isInstanceOf(IllegalArgumentException.class);
  }


  @Test
  void matches_rawAndEncodedPasswordsDontMatch_returnFalse() {
    var rawPassword = FAKER.credentials().password();
    var encodedPassword = new String(BCrypt
        .with(VERSION, LONG_PASSWORD_STRATEGY)
        .hash(ITERATIONS, FAKER.credentials().password().getBytes())
    );
    assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isFalse();
  }

    @Test
  void matches_rawAndEncodedPasswordsMatch_returnTrue() {
    var rawPassword = FAKER.credentials().password();
    var encodedPassword = new String(BCrypt
        .with(VERSION, LONG_PASSWORD_STRATEGY)
        .hash(ITERATIONS, rawPassword.getBytes())
    );
    assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
  }
}
