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
import com.innowise.userservice.controller.CardController;
import com.innowise.userservice.integration.AbstractIntegrationTest;
import com.innowise.userservice.integration.annotation.WebIT;
import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.model.entity.Card;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.model.mapper.CardMapper;
import com.innowise.userservice.testutil.Cards;
import com.innowise.userservice.testutil.Users;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

@WebIT
@RequiredArgsConstructor
class CardControllerIT extends AbstractIntegrationTest {

  private static final String BASE_URL = "/api/v1/cards";

  private static User userFixture;
  private static Card cardFixture;

  private final CardController cardController;

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  private final ObjectMapper jsonMapper;
  private final CardMapper cardMapper;

  private final MockMvc mockMvc;
  @Autowired
  private CacheHelper cacheHelper;

  @BeforeAll
  void prepareFixtures() {
    userFixture = Users.buildWithoutId();
    cardFixture = Cards.buildWithoutId(userFixture);
    transactionTemplate.executeWithoutResult(status -> entityManager.persist(userFixture));
  }

  @AfterAll
  void cleanupFixtures() {
    transactionTemplate.executeWithoutResult(
        status -> entityManager.remove(entityManager.find(User.class, userFixture.getId())));
  }

  @AfterEach
  void cleanupCache() {
    cacheHelper.invalidate();
  }

  @Test
  void contextLoads() {
    assertThat(cardController).isNotNull();
  }

  @Test
  @Transactional
  void create_whenUserExists_shouldReturnCreatedStatusAndCardResponseDto() throws Exception {
    var creatingCard = Cards.build();
    creatingCard.setUser(entityManager.getReference(User.class, userFixture.getId()));
    mockMvc.perform(
        post(BASE_URL + "/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(cardMapper.toDto(creatingCard)))
    ).andExpectAll(
        status().isCreated(),
        content().contentType(MediaType.APPLICATION_JSON),
        jsonPath("$.number").value(creatingCard.getNumber()),
        jsonPath("$.holder").value(creatingCard.getHolder()),
        jsonPath("$.expirationDate").value(creatingCard.getExpirationDate().toString()),
        jsonPath("$.userId").value(userFixture.getId()),
        jsonPath("$.id").isNotEmpty()
    );
  }


  @Test
  @Transactional
  void create_whenUserNotExists_shouldReturnNotFoundStatus() throws Exception {
    var creatingCard = Cards.build();
    creatingCard.setUser(entityManager.getReference(User.class, Long.MAX_VALUE));
    mockMvc.perform(
        post(BASE_URL + "/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(cardMapper.toDto(creatingCard)))
    ).andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  void delete_whenCardNotFound_shouldThrownCardNotFoundException() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/" + Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  void delete_whenCardExists_shouldReturnCard() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/" + cardFixture.getId()))
        .andExpectAll(
            status().isNoContent()
        );
  }

  @Test
  @Transactional
  void update_whenDtoInvalid_shouldReturnBadRequest() throws Exception {
    var invalidDto = CardDto.builder().build();
    mockMvc.perform(put(BASE_URL + "/" + cardFixture.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isUnprocessableContent());
  }

  @Test
  @Transactional
  void update_whenDtoValid_shouldSuccessfullyUpdate() throws Exception {
    var validUpdateDto = CardDto.builder()
        .number(cardFixture.getNumber())
        .holder("Another Bank")
        .expirationDate(cardFixture.getExpirationDate())
        .build();

    var expectedResponseDto = new CardDto(
        cardFixture.getId(),
        cardFixture.getNumber(),
        validUpdateDto.holder(),
        cardFixture.getExpirationDate(),
        cardFixture.getUser().getId()
    );
    mockMvc.perform(put(BASE_URL + "/" + cardFixture.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(validUpdateDto)))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(jsonMapper.writeValueAsString(expectedResponseDto))
        );
  }

  @Test
  void findById_whenCardNotFound_shouldReturnNotFoundStatus() throws Exception {
    mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void findById_whenCardExists_shouldReturnCard() throws Exception {
    mockMvc.perform(get(BASE_URL + "/" + cardFixture.getId()))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(jsonMapper.writeValueAsString(cardMapper.toDto(cardFixture)))
        );
  }

  @Test
  void findAllByIdIn_whenNoOneFound_shouldReturnEmptyList() throws Exception {
    mockMvc.perform(
            get(BASE_URL + "/")
                .param("ids",
                    String.valueOf(Long.MAX_VALUE),
                    String.valueOf(Long.MAX_VALUE - 1)
                )
        )
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(jsonMapper.writeValueAsString(List.of()))
        );
  }

  @Test
  void findAll_whenNoIdsProvided_shouldReturnAllUsers() throws Exception {
    mockMvc.perform(
            get(BASE_URL + "/")
        )
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            content().json(jsonMapper.writeValueAsString(List.of(cardMapper.toDto(cardFixture))))
        );
  }

}
