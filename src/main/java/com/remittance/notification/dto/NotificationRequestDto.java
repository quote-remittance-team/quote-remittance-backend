package com.remittance.notification.dto;

import lombok.Data;
import java.util.UUID;
@Data

public class NotificationRequestDto {
    private UUID userId;
    private String message;
}
