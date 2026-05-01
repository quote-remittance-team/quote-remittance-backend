package com.remittance.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remittance.user.dto.RegisterUserRequest;
import com.remittance.user.dto.UserResponse;
import com.remittance.user.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        UUID userId = UUID.randomUUID();

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        UserResponse response = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .build();

        when(userService.register(any(RegisterUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldReturnBadRequestForInvalidRequest() throws Exception {

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("")
                .password("")
                .build();

        mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnCurrentUserSuccessfully() throws Exception {

        String email = "test@example.com";
        UUID userId = UUID.randomUUID();

        UserResponse response = UserResponse.builder()
                .id(userId)
                .email(email)
                .build();

        when(userService.getUserByEmail(email))
                .thenReturn(response);

        mockMvc.perform(
                        get("/users/me")
                                .param("email", email)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(email));

        verify(userService).getUserByEmail(email);
    }
}
