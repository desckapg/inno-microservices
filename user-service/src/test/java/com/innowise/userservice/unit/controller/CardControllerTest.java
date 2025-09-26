package com.innowise.userservice.unit.controller;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.userservice.controller.CardController;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.exception.GlobalExceptionHandler;
import com.innowise.userservice.model.dto.card.CardDto;
import com.innowise.userservice.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class CardControllerTest {

  private static final String BASE_URL = "/api/v1/cards";

  @Mock
  private CardService cardService;

  @InjectMocks
  private CardController cardController;

  private MockMvc mockMvc;

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void prepare() {
    mockMvc = MockMvcBuilders.standaloneSetup(cardController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void delete_whenCardNotFound_shouldThrownCardNotFoundException() throws Exception {
    doThrow(ResourceNotFoundException.byField("Card", "id", 1L))
        .when(cardService)
        .delete(1L);
    mockMvc.perform(delete(BASE_URL + "/1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_whenCardExists_shouldReturnNoContent() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/1"))
        .andExpect(status().isNoContent());
  }

  @Test
  void update_whenDtoInvalid_shouldReturnBadRequest() throws Exception {
    var invalidDto = CardDto.builder().build();

    mockMvc.perform(put(BASE_URL + "/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(invalidDto)))
        .andExpect(status().isUnprocessableContent());
  }

}
