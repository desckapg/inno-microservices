package com.innowise.authservice.exception;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import java.io.Serial;
import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class TokenException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 2201998854758646992L;

  private final TokenErrorCode errorCode;
  private final OffsetDateTime occurredAt;

  public TokenException(TokenErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.occurredAt = OffsetDateTime.now();
  }

  public static TokenException expired(Instant expiresAt) {
    String message = String.format("Token expired at %s", expiresAt);
    return new TokenException(
        TokenErrorCode.EXPIRED, message
    );
  }

  public static TokenException malformed(String reason) {
    String message = String.format("Token is malformed: %s", reason);
    return new TokenException(
        TokenErrorCode.MALFORMED, message
    );
  }

  public static TokenException signatureInvalid() {
    return new TokenException(
        TokenErrorCode.INVALID_SIGNATURE,
        "Token signature is invalid"
    );
  }

  public static TokenException unsupported() {
    return new TokenException(
        TokenErrorCode.UNSUPPORTED,
        "Unsupported JWT algorithm"
    );
  }

  public static TokenException missing() {
    return new TokenException(
        TokenErrorCode.MISSING,
        "Token must be provided"
    );
  }

  public static TokenException fromJwtException(JWTVerificationException ex) {
    return switch (ex) {
      case TokenExpiredException expiredEx -> expired(expiredEx.getExpiredOn());
      case JWTDecodeException _ -> malformed("Malformed JWT structure");
      case SignatureVerificationException _ -> signatureInvalid();
      case AlgorithmMismatchException _ -> unsupported();
      default -> new TokenException(
          TokenErrorCode.INVALID,
          "Token validation failed"
      );
    };
  }

  @Getter
  public enum TokenErrorCode {
    EXPIRED(HttpStatus.UNAUTHORIZED, "token_expired"),
    MALFORMED(HttpStatus.UNPROCESSABLE_CONTENT, "token_malformed"),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "invalid_signature"),
    UNSUPPORTED(HttpStatus.UNPROCESSABLE_CONTENT, "unsupported_token"),
    MISSING(HttpStatus.UNPROCESSABLE_CONTENT, "missing_token"),
    INVALID(HttpStatus.UNAUTHORIZED, "invalid_token");

    private final HttpStatus httpStatus;
    private final String code;

    TokenErrorCode(HttpStatus httpStatus, String code) {
      this.httpStatus = httpStatus;
      this.code = code;
    }

  }

}
