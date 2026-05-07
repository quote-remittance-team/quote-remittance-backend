package com.remittance.auth.controller;

import com.remittance.auth.dto.AuthResponse;
import com.remittance.auth.dto.LoginRequest;
import com.remittance.auth.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        log.info("Login request received for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        log.info("Login successful for email: {}", request.getEmail());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
