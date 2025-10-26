package com.innowise.common.exception;

import com.innowise.common.model.dto.ErrorDto;
import java.io.Serial;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

public class ExternalApiException extends RuntimeException {

  @Getter
  private @Nullable ErrorDto errorDto;

  @Serial
  private static final long serialVersionUID = -2076544813962210621L;

  public ExternalApiException(String message, ErrorDto errorDto) {
    super(message);
    this.errorDto = errorDto;
  }

  public ExternalApiException(String message) {
    super(message);
  }

  public ExternalApiException(@Nullable ErrorDto errorDto) {
    this.errorDto = errorDto;
  }
}
