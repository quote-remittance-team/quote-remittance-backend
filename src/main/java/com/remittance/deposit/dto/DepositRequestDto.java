package com.remittance.deposit.dto;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import java.util.UUID;

@Data
public class DepositRequestDto {
    private UUID quoteId;
    @NotBlank(message = "Idempotency key is strictly required to prevent duplicate transaction")
    private String idempotencyKey;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver account number is required")
    private String receiverAccountNumber;

    @NotBlank(message = "Receiver bank code is required")
    private String receiverBankCode;
}
