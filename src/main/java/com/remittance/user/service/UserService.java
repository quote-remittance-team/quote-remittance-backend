package com.remittance.user.service;

import com.remittance.user.dto.RegisterUserRequest;
import com.remittance.user.dto.UserResponse;

public interface UserService {

    UserResponse register(RegisterUserRequest request);
    UserResponse getUserByEmail(String email);
}
