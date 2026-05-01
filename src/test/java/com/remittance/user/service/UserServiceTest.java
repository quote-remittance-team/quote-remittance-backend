package com.remittance.user.service;

import com.remittance.common.exception.UserNotFoundException;
import com.remittance.user.dto.RegisterUserRequest;
import com.remittance.user.dto.UserResponse;
import com.remittance.user.entity.User;
import com.remittance.user.repository.UserRepository;
import com.remittance.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        User savedUser = User.builder()
                .email("test@example.com")
                .passwordHash("hashed-password")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, never()).save(any());
    }


    @Test
    void shouldGetUserByEmailSuccessfully() {

        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashed-password")
                .build();

        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByEmail("test@example.com");

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());

        verify(userRepository).findByEmailIgnoreCase("test@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {

        when(userRepository.findByEmailIgnoreCase("missing@example.com"))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByEmail("missing@example.com")
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmailIgnoreCase("missing@example.com");
    }
}
