package com.innowise.userservice.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.userservice.cache.CacheHelper;
import com.innowise.userservice.controller.UserController;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.WebIT;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.model.mapper.UserMapper;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

@WebIT
@RequiredArgsConstructor
class UserControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/users";

  private User userFixture;

  private final UserController userController;

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  private final MockMvc mockMvc;

  private final ObjectMapper jsonMapper;
  private final UserMapper userMapper;
  private final CardMapper cardMapper;

  @Autowired
  private CacheHelper cacheHelper;

  @BeforeAll
  void prepareFixtures() {
    userFixture = Users.buildWithoutId();
    Cards.buildWithoutId(userFixture);
    transactionTemplate.executeWithoutResult(status ->
        entityManager.persist(userFixture));
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(status ->
        entityManager.remove(entityManager.find(User.class, userFixture.getId())));
  }

  @BeforeEach
  void cleanupCache() {
    cacheHelper.invalidate();
  }

  @Test
  void contextLoads() {
    assertThat(userController).isNotNull();
  }

  @Test
  void findById_whenUserNotExists_shouldReturnNotFoundStatus() throws Exception {
    mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void findById_withCardsWhenUserNotExists_shouldReturnNotFoundStatus() throws Exception {
    mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE)
            .queryParam("includeCards", "true"))
        .andExpect(status().isNotFound());
  }

  @Test
  void findById_withoutCardsWhenUserExists_shouldUserResponseDto() throws Exception {
    mockMvc.perform(get(BASE_URL + "/" + userFixture.getId()))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(jsonMapper.writeValueAsString(userMapper.toDto(userFixture)))
        );
  }

  @Test
  void findById_withCardsWhenUserExists_shouldReturnUserResponseDtoWithCards() throws Exception {
    mockMvc.perform(get(BASE_URL + "/" + userFixture.getId())
            .queryParam("includeCards", "true"))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(jsonMapper.writeValueAsString(userMapper.toDto(userFixture)))
        );
  }

  @Test
  void findByEmail_withoutCardsWhenUserNotExists_shouldReturnNotFoundStatus() throws Exception {
    mockMvc.perform(get(BASE_URL + "/email/nonexistent@example.com"))
        .andExpect(status().isNotFound());
  }

  @Test
  void findByEmail_withCardsWhenUserNotExists_shouldReturnNotFoundStatus() throws Exception {
    mockMvc.perform(get(BASE_URL + "/email/nonexistent@example.com")
            .queryParam("includeCards", "true"))
        .andExpect(status().isNotFound());
  }

  @Test
  void findByEmail_whenUserExists_shouldUserResponseDto() throws Exception {
    mockMvc.perform(get(BASE_URL)
            .param("email", userFixture.getEmail()))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(jsonMapper.writeValueAsString(List.of(userMapper.toDto(userFixture))))
        );
  }

  @Test
  void findUsersCards_whenUserExists_shouldReturnListOfCardResponseDto() throws Exception {
    mockMvc.perform(get(BASE_URL + "/" + userFixture.getId() + "/cards"))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(jsonMapper.writeValueAsString(
                userFixture.getCards().stream()
                    .map(cardMapper::toDto)
                    .toList()
            ))
        );
  }

  @Test
  void findUsersCards_whenUserNotExists_shouldReturnNotFoundStatus() throws Exception {
    mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE + "/cards"))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  void create_shouldReturnCreatedStatusAndUserResponseDto() throws Exception {
    var creatingUser = Users.build();
    mockMvc.perform(
        post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(UserDto.builder()
                .name(creatingUser.getName())
                .surname(creatingUser.getSurname())
                .birthDate(creatingUser.getBirthDate())
                .email(creatingUser.getEmail())
                .build()
            ))
    ).andExpectAll(
        status().isCreated(),
        content().contentType(MediaType.APPLICATION_JSON)
    );
  }

  @Test
  @Transactional
  void update_whenInvalidDto_shouldThrowValidationException() throws Exception {
    var updatingDto = UserDto.builder().build();
    mockMvc.perform(
        put(BASE_URL + "/" + Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(updatingDto))
    ).andExpect(status().isUnprocessableContent());
  }

  @Test
  @Transactional
  void update_whenUserNotExists_shouldReturnNotFoundStatus() throws Exception {
    var updatingUser = Users.build();
    mockMvc.perform(
        put(BASE_URL + "/" + Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(UserDto.builder()
                .name(updatingUser.getName())
                .surname(updatingUser.getSurname())
                .birthDate(updatingUser.getBirthDate())
                .email(updatingUser.getEmail())
                .build()
            ))
    ).andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  void update_whenExists_shouldReturnUpdateUserResponseDto() throws Exception {
    var updatingUser = userFixture;
    mockMvc.perform(
        put(BASE_URL + "/" + updatingUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(UserDto.builder()
                .name(updatingUser.getName() + "_updated")
                .surname(updatingUser.getSurname() + "_updated")
                .birthDate(updatingUser.getBirthDate())
                .email(updatingUser.getEmail())
                .build()
            ))
    ).andExpectAll(
        status().isOk(),
        content().contentType(MediaType.APPLICATION_JSON),
        jsonPath("$.name").value(updatingUser.getName() + "_updated"),
        jsonPath("$.surname").value(updatingUser.getSurname() + "_updated"),
        jsonPath("$.email").value(updatingUser.getEmail()),
        jsonPath("$.birthDate").value(updatingUser.getBirthDate().toString()),
        jsonPath("$.id").value(updatingUser.getId())
    );
  }

  @Test
  @Transactional
  void delete_whenUserNotExists_shouldReturnNotFoundStatus() throws Exception {
    mockMvc.perform(
        delete(BASE_URL + "/" + Long.MAX_VALUE)
    ).andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  void delete_whenUserExists_shouldReturnNoContentStatus() throws Exception {
    mockMvc.perform(
        delete(BASE_URL + "/" + userFixture.getId())
    ).andExpect(status().isNoContent());
  }


}
