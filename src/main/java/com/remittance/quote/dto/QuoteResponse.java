package com.remittance.quote.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class QuoteResponse {

    private UUID quoteId;

    private String quoteReference;

    private BigDecimal sendAmount;

    private String fromCurrency;

    private String toCurrency;

    private BigDecimal exchangeRate;

    private BigDecimal fee;

    private BigDecimal receiveAmount;

    private BigDecimal totalPayable;

    private Instant expiresAt;
}
