package com.remittance.deposit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.remittance.enums.DepositStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepositWebhookDto {
    @NotBlank(message = "Payment reference cannot be blank")
    private String paymentReference;

    @NotNull(message = "Deposit status is required")
    @JsonProperty("Status")
    private DepositStatus status;
}


