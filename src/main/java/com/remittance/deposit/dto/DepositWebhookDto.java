package com.remittance.deposit.dto;

import com.remittance.enums.DepositStatus;
import lombok.Data;

@Data
public class DepositWebhookDto {
    private String paymentReference;
    private DepositStatus status;
}
