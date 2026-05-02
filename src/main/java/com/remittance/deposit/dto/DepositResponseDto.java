package com.remittance.deposit.dto;

import com.remittance.enums.DepositStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class DepositResponseDto {
    private UUID id;
    private DepositStatus status;
    private BigDecimal amount;
    private String currency;
    private String checkoutUrl;
}
