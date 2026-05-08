package com.remittance.integration.payment.dto;

import lombok.Builder;
import lombok.Data;
@Data
@Builder

public class PaystackInitializeRequestDto {
    private String email;
    private String amount;
    private String reference;
}
