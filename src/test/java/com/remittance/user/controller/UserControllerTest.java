package com.remittance.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remittance.auth.security.JwtAuthenticationFilter;
import com.remittance.auth.security.JwtService;
import com.remittance.user.dto.RegisterUserRequest;
import com.remittance.user.dto.UserResponse;
import com.remittance.user.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )
        }
)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
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
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidRequest() throws Exception {

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("")
                .password("")
                .build();

        mockMvc.perform(
                        post("/users/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnCurrentUserSuccessfully() throws Exception {

        UUID userId = UUID.randomUUID();

        UserResponse response = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .build();

        when(userService.getUserByEmail("test@example.com"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/users/me")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserByEmail("test@example.com");
    }
}
