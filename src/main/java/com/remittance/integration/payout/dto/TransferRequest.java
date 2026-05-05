package com.remittance.integration.payout.dto;

public record TransferRequest (
        String source,
        Long amount,
        String recipient,
        String reason
) {}
