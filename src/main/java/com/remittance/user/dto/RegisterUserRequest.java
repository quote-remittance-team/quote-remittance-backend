package com.remittance.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterUserRequest {

    private String email;

    private String password;
}
