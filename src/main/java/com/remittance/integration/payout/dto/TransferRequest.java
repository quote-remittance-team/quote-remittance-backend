package com.remittance.integration.payout.dto;

public record TransferRequest (
        String source,
        Integer amount,
        String recipient,
        String reason
) {}
