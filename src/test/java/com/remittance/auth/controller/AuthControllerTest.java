package com.remittance.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.remittance.auth.dto.AuthResponse;
import com.remittance.auth.dto.LoginRequest;

import com.remittance.auth.service.AuthService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // IMPORTANT
    @MockBean
    private com.remittance.auth.security.JwtService jwtService;

    @MockBean
    private com.remittance.auth.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void login_ShouldReturn200Ok_WithJwtToken() throws Exception {

        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("mock-jwt-token")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("mock-jwt-token"));

        verify(authService, times(1))
                .login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturn400BadRequest_WhenPayloadIsInvalid()
            throws Exception {

        LoginRequest request = LoginRequest.builder()
                .email("")
                .password("")
                .build();

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(authService, times(0))
                .login(any(LoginRequest.class));
    }
}
