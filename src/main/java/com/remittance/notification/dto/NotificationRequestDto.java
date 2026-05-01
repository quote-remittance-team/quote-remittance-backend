package com.remittance.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDto {

    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @NotBlank(message = "Message cannot be blank")
    private String message;
}
