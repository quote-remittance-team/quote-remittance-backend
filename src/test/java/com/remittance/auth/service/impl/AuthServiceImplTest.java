package com.remittance.auth.service.impl;

import com.remittance.auth.dto.AuthResponse;
import com.remittance.auth.dto.LoginRequest;
import com.remittance.auth.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void login_ShouldReturnAuthResponseWithJwtToken() {

        String expectedToken = "mock-jwt-token";

        when(jwtService.generateToken(loginRequest.getEmail()))
                .thenReturn(expectedToken);

        AuthResponse response = authService.login(loginRequest);

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService, times(1))
                .generateToken(loginRequest.getEmail());

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
    }
}
