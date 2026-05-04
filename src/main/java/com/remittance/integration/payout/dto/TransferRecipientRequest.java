package com.remittance.integration.payout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransferRecipientRequest(
    String type,
    String name,

    @JsonProperty("account_number")
    String accountNumber,

    @JsonProperty("bank_code")
    String bankCode,
    String currency
) {}
