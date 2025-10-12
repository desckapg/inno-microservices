package com.innowise.authservice.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Version;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategy;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomBCryptPasswordEncoder implements PasswordEncoder {

  private static final SecureRandom RANDOM = new SecureRandom();

  private static final int SALT_LENGTH = 16;

  private static final BCrypt.Version VERSION = Version.VERSION_2Y;
  private static final LongPasswordStrategy LONG_PASSWORD_STRATEGY =
      LongPasswordStrategies.hashSha512(VERSION);
  private static final int ITERATIONS = 12;


  @Override
  public boolean upgradeEncoding(@Nullable String encodedPassword) {
    return false;
  }

  @Override
  public String encode(@Nullable CharSequence rawPassword) {
    if (rawPassword == null) {
      throw new IllegalArgumentException("RawPassword cannot be null");
    }
    return new String(BCrypt
        .with(VERSION, RANDOM, LONG_PASSWORD_STRATEGY)
        .hash(ITERATIONS, generateSalt(), rawPassword.toString().getBytes())
    );
  }

  @Override
  public boolean matches(@Nullable CharSequence rawPassword, @Nullable String encodedPassword) {
    if (rawPassword == null) {
      throw new IllegalArgumentException("RawPassword cannot be null");
    }
    if (encodedPassword == null) {
      throw new IllegalArgumentException("EncodedPassword cannot be null");
    }
    return BCrypt
        .verifyer(VERSION, LONG_PASSWORD_STRATEGY)
        .verify(rawPassword.toString().getBytes(StandardCharsets.UTF_8), encodedPassword.getBytes())
        .verified;
  }

  private byte[] generateSalt() {
    byte[] salt = new byte[SALT_LENGTH];
    RANDOM.nextBytes(salt);
    return salt;
  }
}
