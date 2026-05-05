package com.remittance.integration.payout.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransferRecipientResponse (
        boolean status,
        Data data
) {
    public record Data(
            @JsonProperty("recipient_code")
            String recipientCode
    ) {}
}
