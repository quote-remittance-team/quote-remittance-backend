package com.remittance.integration.payout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransferResponse (
        boolean status,
        String message,
        Data data
) {
    public record Data (
            @JsonProperty("transfer_code")
            String transferCode,
            String status
    ){}
}
