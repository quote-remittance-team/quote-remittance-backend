package com.remittance.deposit.dto;

import com.remittance.enums.DepositStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepositWebhookDto {
    private String paymentReference;
    private DepositStatus status;
    @NotNull(message = "Deposit status is required")
    private DepositStatus depositStatus;
}
