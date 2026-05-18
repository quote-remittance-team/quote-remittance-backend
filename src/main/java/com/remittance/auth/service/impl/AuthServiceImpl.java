package com.remittance.auth.service.impl;

import com.remittance.auth.dto.*;
import com.remittance.auth.service.AuthService;
import com.remittance.auth.security.JwtService;
import com.remittance.user.entity.User;
import com.remittance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token =
                jwtService.generateToken(authentication.getName());

        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .build();
    }
}