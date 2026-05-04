package com.remittance.integration.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class PaystackWebhookEventDto {
    private String event;
    private WebhookData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookData {
        private String reference;
        private String status;
        private String amount;
    }
}
