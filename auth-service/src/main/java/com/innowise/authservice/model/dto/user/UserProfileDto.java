package com.innowise.authservice.model.dto.user;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Builder
@Getter
@AllArgsConstructor
public class UserProfileDto {

  @Setter
  private Long id;

  private String name;

  private String surname;

  private LocalDate birthDate;

  private String email;

}
