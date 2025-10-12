package com.innowise.userservice.unit.controller;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.userservice.controller.UserController;
import com.innowise.userservice.exception.GlobalExceptionHandler;
import com.innowise.common.exception.ResourceNotFoundException;
import com.innowise.userservice.model.dto.user.UserDto;
import com.innowise.userservice.service.UserService;
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
class UserControllerTest {

  private static final String BASE_URL = "/api/v1/users";

  @Mock
  private UserService userService;

  @InjectMocks
  private UserController userController;

  private MockMvc mockMvc;

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void prepare() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void delete_whenUserNotFound_shouldThrownResourceNotFoundException() throws Exception {
    doThrow(ResourceNotFoundException.byField("User", "id", 1L))
        .when(userService)
        .delete(1L);
    mockMvc.perform(delete(BASE_URL + "/1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_whenUserExists_shouldReturnNoContent() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/1"))
        .andExpect(status().isNoContent());
  }

  @Test
  void update_whenDtoInvalid_shouldReturnBadRequest() throws Exception {
    var invalidDto = UserDto.builder().build();

    mockMvc.perform(put(BASE_URL + "/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(invalidDto)))
        .andExpect(status().isUnprocessableContent());
  }

}
