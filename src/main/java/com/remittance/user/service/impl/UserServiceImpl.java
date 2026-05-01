package com.remittance.user.service.impl;

import com.remittance.user.dto.RegisterUserRequest;
import com.remittance.user.dto.UserResponse;
import com.remittance.user.entity.User;
import com.remittance.user.repository.UserRepository;
import com.remittance.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {

        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        try {

            User user = User.builder()
                    .email(normalizedEmail)
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .build();

            User savedUser = userRepository.save(user);

            return UserResponse.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .build();
        } catch (DataIntegrityViolationException ex) {

            throw new IllegalArgumentException("Email already exists");
        }
    }

    @Override
    public UserResponse getUserByEmail(String email) {

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

}
