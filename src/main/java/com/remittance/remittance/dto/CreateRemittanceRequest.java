package com.remittance.remittance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRemittanceRequest {

    @NotNull(message = "Deposit ID is required")
    private UUID depositId;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver account number is required")
    private String receiverAccountNumber;

    @NotBlank(message = "Receiver bank code is required")
    private String receiverBankCode;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
