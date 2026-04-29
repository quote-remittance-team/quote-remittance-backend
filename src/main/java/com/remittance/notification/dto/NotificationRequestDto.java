package com.remittance.notification.dto;

import com.remittance.enums.NotificationStatus;
import lombok.Data;
import java.util.UUID;
@Data

public class NotificationRequestDto {
    private UUID userId;
    private String message;
}
