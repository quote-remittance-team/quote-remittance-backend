package com.remittance.auth.service;

import com.remittance.auth.dto.*;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}