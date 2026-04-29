package com.remittance.deposit.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class DepositRequestDto {
    private UUID quoteId;
    private String idempotencyKey;
}
