package com.remittance.remittance.dto;

import com.remittance.enums.RemittanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RemittanceResponse {

    private UUID remittanceId;

    private String reference;

    private BigDecimal sendAmount;

    private BigDecimal receiveAmount;

    private RemittanceStatus status;

    private Instant createdAt;
}
